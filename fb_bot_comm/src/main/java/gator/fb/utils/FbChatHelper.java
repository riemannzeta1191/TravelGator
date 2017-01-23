package gator.fb.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gator.fb.contract.Attachment;
import gator.fb.contract.Button;
import gator.fb.contract.DefaultAction;
import gator.fb.contract.Element;
import gator.fb.contract.Message;
import gator.fb.contract.Messaging;
import gator.fb.contract.Payload;
import gator.fb.contract.QuickReply;
import gator.fb.contract.Recipient;
import gator.fb.profile.FbProfile;
import gator.fb.servlet.WebHookServlet;
import gator.google.contract.NearbyResponse;
import gator.google.contract.Result;
import gator.google.places.PlacesAPI;
import gator.utils.APIConstants;
import gator.utils.Constants;

/**
 * 
 * @author takirala
 *
 */
public class FbChatHelper {

	public String getWelcomeMsg(String senderId) {
		String link = WebHookServlet.getPROFILE_URL(senderId);
		FbProfile profile = getObjectFromUrl(link, FbProfile.class);
		Message msg = getMsg(Constants.welcomeMessage.replace("{0}", profile.getFirstName()));
		List<QuickReply> quick_replies = new ArrayList<>();
		QuickReply qr = new QuickReply();
		qr.setContent_type(Constants.Types.location.name());
		quick_replies.add(qr);

		msg.setQuick_replies(quick_replies);
		return getJsonReply(senderId, msg);
	}

	public List<String> getGooglePlacesRes(String senderId, double latitude, double longitude,
			ConcurrentHashMap<String, NearbyResponse> userRecommendations) {
		if (Constants.isDebugEnabled)
			System.out.println("Get google res for : " + senderId);

		List<String> jsonReplies = new ArrayList<String>();

		try {
			NearbyResponse nearbyResponse = userRecommendations.get(senderId);
			if (nearbyResponse == null) {
				nearbyResponse = PlacesAPI.getNearbyPlaces(latitude, longitude);
				userRecommendations.put(senderId, nearbyResponse);
			}
			jsonReplies = sendNextItemToBot(senderId, 0, userRecommendations);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonReplies;
	}

	public String getTypingStatus(String senderId) {
		JsonObject obj = new JsonObject();
		obj.addProperty("sender_action", "typing_on");
		JsonObject recp = new JsonObject();
		recp.addProperty("id", senderId);
		obj.add("recipient", recp);
		return obj.toString();
	}

	public List<String> processUserRecommendations(String senderId, String payloadMessage,
			ConcurrentHashMap<String, NearbyResponse> userRecommendations) {
		String[] arr = payloadMessage.split(" ");

		List<String> jsonReplies = new ArrayList<>();

		if (arr[0].equals("placeId_res")) {
			String placeId = arr[2];

			NearbyResponse nearbyRes = userRecommendations.get(senderId);
			List<Result> results = nearbyRes.getResults();

			boolean removeIndex = false;
			Integer foundAt = null;
			for (int i = 0; i < results.size(); i++) {

				Result r = results.get(i);

				if (r.getPlace_id().equals(placeId)) {
					foundAt = i;
					removeIndex = arr[1].equals("YES");
					break;
				}
			}
			if (foundAt == results.size() - 1) {
				// Send list as response
				// build url route map.
				if (removeIndex)
					results.remove(foundAt);

				List<Element> elements = new ArrayList<>();

				for (int i = 0; i <= 3; i++) {
					elements.add(buildElement(results.get(i)));
				}

				Message attachMsg = new Message();
				attachMsg.setAttachment(new Attachment());
				attachMsg.getAttachment().setType(Constants.Types.template.name());
				attachMsg.getAttachment().setPayload(new Payload());
				attachMsg.getAttachment().getPayload().setTemplateType(Constants.Types.list.name());
				attachMsg.getAttachment().getPayload().setElements(elements);
				attachMsg.getAttachment().getPayload()
						.setButtons(addLoadButton(Constants.Types.postback.name(), "LoadMore", "See More!"));
				jsonReplies.add(getJsonReply(senderId, attachMsg));

			} else {
				// send next item as response
				jsonReplies = sendNextItemToBot(senderId, foundAt + 1, userRecommendations);
			}

			if (removeIndex)
				results.remove(foundAt);

		}

		return jsonReplies;
	}

	public List<String> processFinalRoute(String senderId,
			ConcurrentHashMap<String, NearbyResponse> userRecommendations) {
		List<String> jsonReplies = new ArrayList<>();

		NearbyResponse nearbyRes = userRecommendations.get(senderId);
		List<Result> results = nearbyRes.getResults();

		List<Element> elements = new ArrayList<>();

		for (int i = 4; i <= 7 && i < results.size(); i++) {
			elements.add(buildElement(results.get(i)));
		}

		Message attachMsg = new Message();
		attachMsg.setAttachment(new Attachment());
		attachMsg.getAttachment().setType(Constants.Types.template.name());
		attachMsg.getAttachment().setPayload(new Payload());
		attachMsg.getAttachment().getPayload().setTemplateType(Constants.Types.list.name());
		attachMsg.getAttachment().getPayload().setElements(elements);

		List<Button> routeButtons = new ArrayList<>();
		Button routeButton = new Button();
		routeButton.setTitle("View Route");
		routeButton.setType(Constants.Types.web_url.name());
		routeButton.setUrl(PlacesAPI.getDirections(nearbyRes));
		routeButtons.add(routeButton);
		attachMsg.getAttachment().getPayload().setButtons(routeButtons);

		jsonReplies.add(getJsonReply(senderId, attachMsg));
		return jsonReplies;
	}

	private Message getMsg(String msg) {
		Message message = new Message();
		message.setText(msg);
		return message;
	}

	/**
	 * final body which will be sent to fb messenger api through a post call
	 * 
	 * @see WebHookServlet#FB_MSG_URL
	 * @param senderId
	 * @param message
	 * @return
	 */
	private String getJsonReply(String senderId, Message message) {
		Messaging reply = new Messaging();
		Recipient recipient = new Recipient();
		recipient.setId(senderId);
		reply.setRecipient(recipient);
		reply.setMessage(message);

		String jsonReply = new Gson().toJson(reply);
		return jsonReply;
	}

	/**
	 * Returns object of type clazz from an json api link
	 * 
	 * @param link
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	private <T> T getObjectFromUrl(String link, Class<T> clazz) {
		T t = null;
		URL url;
		String jsonString = "";
		try {
			url = new URL(link);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				jsonString = jsonString + inputLine;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!StringUtils.isEmpty(jsonString)) {
			Gson gson = new Gson();
			t = gson.fromJson(jsonString, clazz);
		}
		return t;
	}

	private Element buildRegularElement(Result r) {
		Element e = buildElement(r);
		e.setButtons(addButton(r.getPlace_id()));
		return e;
	}

	private Element buildElement(Result r) {
		Element e = new Element();
		e.setTitle(r.getName());
		if (r.getPhotos() != null && r.getPhotos().size() > 0) {
			try {
				URLConnection conn = (new URL(APIConstants.getPhotoURL(r.getPhotos().get(0).getPhotoreference())))
						.openConnection();
				conn.setAllowUserInteraction(true);
				conn.connect();
				conn.getInputStream();
				e.setImageUrl(conn.getURL().toString());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else {
			e.setImageUrl(r.getIcon());
		}
		e.setSubtitle(getSubtitle(r));
		DefaultAction default_action = new DefaultAction();
		default_action.setType(Constants.Types.web_url.name());
		default_action.setWebview_height_ratio(Constants.Heights.full.name());
		default_action.setUrl(r.getUrl() == null ? r.getIcon() : r.getUrl());
		e.setDefault_action(default_action);
		return e;
	}

	private String getDescription(Result r) {
		String res = r.getName()
				+ " is located in the vicinity of {vicinity}. Rated {rating} and has {reviews} reviews. ";
		Random rand = new Random();
		res = res.replace("{vicinity}", r.getVicinity());
		res = res.replace("{rating}", "" + (r.getRating() == 0.0 ? (rand.nextInt(24) + 26) / 10.0 : r.getRating()));
		res = res.replace("{reviews}", "" + rand.nextInt(100));
		return res;
	}

	private String getSubtitle(Result r) {

		String res = "People generally spend around {timeSpent} hour here.";
		res = res.replace("{timeSpent}", "" + (int) (new Random().nextInt(4) + 2) / 2.0);
		return res;
	}

	private List<Button> addLoadButton(String type, String payload, String title) {
		List<Button> buttons = new ArrayList<>();

		Button b1 = new Button();
		b1.setTitle(title);
		b1.setPayload(payload);
		b1.setType(type);
		buttons.add(b1);
		return buttons;
	}

	private List<Button> addButton(String place_id) {
		List<Button> buttons = new ArrayList<>();
		Button b1 = new Button();
		b1.setTitle("Cool");
		b1.setPayload("placeId_res YES " + place_id);

		b1.setType(Constants.Types.postback.name());
		Button b2 = new Button();
		b2.setTitle("Maybe");
		b2.setPayload("placeId_res MAY " + place_id);
		b2.setType(Constants.Types.postback.name());

		Button b3 = new Button();
		b3.setTitle("Meh");
		b3.setPayload("placeId_res NO " + place_id);
		b3.setType(Constants.Types.postback.name());

		buttons.add(b1);
		buttons.add(b2);
		buttons.add(b3);
		return buttons;
	}

	private List<String> sendNextItemToBot(String senderId, int index,
			ConcurrentHashMap<String, NearbyResponse> userRecommendations) {
		List<String> jsonReplies = new ArrayList<String>();

		NearbyResponse nearbyResponse = userRecommendations.get(senderId);
		List<Result> res = nearbyResponse.getResults();
		List<Element> elems = new ArrayList<Element>();
		Message textMsg = new Message();

		Result next = res.get(index);
		elems.add(buildRegularElement(next));
		textMsg.setText(getDescription(next));
		jsonReplies.add(getJsonReply(senderId, textMsg));

		Message attachMsg = new Message();
		attachMsg.setAttachment(new Attachment());
		attachMsg.getAttachment().setType(Constants.Types.template.name());
		attachMsg.getAttachment().setPayload(new Payload());
		attachMsg.getAttachment().getPayload().setTemplateType(Constants.Types.generic.name());
		attachMsg.getAttachment().getPayload().setElements(elems);
		jsonReplies.add(getJsonReply(senderId, attachMsg));

		return jsonReplies;
	}

}
