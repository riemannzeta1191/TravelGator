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

/**
 * A Utility class which replies to fb messgaes (or postbacks).<br/>
 * If you already have a service which takes string as input and gives some
 * output, you can easily embed that service in this utility to make your own AI
 * bot.<br/>
 * 
 * A Service can be a search engine for music or a help desk of a company.
 * 
 * @author takirala
 *
 */
public class FbChatHelper {
	private String vr = "VR headsets";
	private List<TitleSubTitle> vrImageUrls;
	private static String profileLink = "https://graph.facebook.com/v2.6/SENDER_ID?access_token="
			+ WebHookServlet.PAGE_TOKEN;

	public FbChatHelper() {
		vrImageUrls = new ArrayList<>();

		TitleSubTitle strollup = new TitleSubTitle();
		strollup.setUrl("https://static.strollup.in/image/191/100/StrollUp-FB-Logo-200x200.jpg");
		strollup.setTitle("StrollUp");
		strollup.setSubTitle("Explore best events and activities over chat");
		vrImageUrls.add(strollup);

		TitleSubTitle rift = new TitleSubTitle();
		rift.setUrl("http://messengerdemo.parseapp.com/img/rift.png");
		rift.setTitle("Rift VR");
		rift.setSubTitle("Rift VR is the only virtual reality head gear you'll ever need");
		vrImageUrls.add(rift);

		TitleSubTitle gearVr = new TitleSubTitle();
		gearVr.setUrl("http://messengerdemo.parseapp.com/img/gearvr.png");
		gearVr.setTitle("Gear VR");
		gearVr.setSubTitle("Gear VR is not just the best, it's better than the best");
		vrImageUrls.add(gearVr);
	}

	private class TitleSubTitle {
		private String url;
		private String title;
		private String subTitle;

		public void setUrl(String url) {
			this.url = url;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setSubTitle(String subTitle) {
			this.subTitle = subTitle;
		}
	}

	/**
	 * methods which analyze the postbacks ie. the button clicks sent by
	 * senderId and replies according to it.
	 * 
	 * @param senderId
	 * @param text
	 * @return
	 */
	public List<String> getPostBackReplies(String senderId, String text) {
		List<String> postbackReplies = new ArrayList<String>();
		String msg = "received postback msg: " + text;

		Message fbMsg = getMsg(msg);
		String fbReply = getJsonReply(senderId, fbMsg);
		postbackReplies.add(fbReply);

		Message vrCards = getVRCards();
		String fbVrCards = getJsonReply(senderId, vrCards);
		postbackReplies.add(fbVrCards);

		return postbackReplies;
	}

	/**
	 * methos which analyze the simple texts sent by senderId and replies
	 * according to it.
	 * 
	 * @param senderId
	 * @param text
	 * @return
	 */
	public List<String> getReplies(String senderId, String text) {
		List<String> replies = new ArrayList<String>();
		String link = StringUtils.replace(profileLink, "SENDER_ID", senderId);
		FbProfile profile = getObjectFromUrl(link, FbProfile.class);

		String msg = "Hello " + profile.getFirstName() + ", I've received msg: " + text;
		Message fbMsg = getMsg(msg);
		String fbReply = getJsonReply(senderId, fbMsg);
		replies.add(fbReply);

		Message question = getProductQuestion();
		String fbQuestion = getJsonReply(senderId, question);
		replies.add(fbQuestion);

		return replies;
	}

	public String getWelcomeMsg(String senderId) {
		String link = StringUtils.replace(profileLink, "SENDER_ID", senderId);
		FbProfile profile = getObjectFromUrl(link, FbProfile.class);
		Message msg = getMsg(Constants.welcomeMessage.replace("{0}", profile.getFirstName()));
		List<QuickReply> quick_replies = new ArrayList<>();
		QuickReply qr = new QuickReply();
		qr.setContent_type(Constants.Types.location.name());
		quick_replies.add(qr);
		msg.setQuick_replies(quick_replies);
		return getJsonReply(senderId, msg);
	}

	private Message getMsg(String msg) {
		Message message = new Message();
		message.setText(msg);
		return message;
	}

	private Message getProductQuestion() {

		String questionStr = "Which product would you like to see?";
		List<Button> buttons = getOptionButtons();

		Payload payload = new Payload();
		payload.setText(questionStr);
		payload.setButtons(buttons);
		payload.setTemplateType("button");

		return getQuestion(payload);
	}

	private List<Button> getOptionButtons() {
		List<Button> buttons = new ArrayList<>();

		Button strollupBot = new Button();
		buttons.add(strollupBot);
		strollupBot.setType("web_url");
		strollupBot.setTitle("StrollUp Bot");
		strollupBot.setUrl("https://www.strollup.in/#!/newchat");

		Button option = new Button();
		buttons.add(option);
		option.setType("postback");
		option.setTitle(vr);
		option.setPayload(vr);

		Button showDetail = new Button();
		buttons.add(showDetail);
		showDetail.setType("web_url");
		showDetail.setTitle("Fb Project");
		showDetail.setUrl("https://github.com/thekosmix/Java-FbChatBot");

		return buttons;
	}

	private Message getQuestion(Payload payload) {
		Attachment attachment = new Attachment();
		attachment.setPayload(payload);
		attachment.setType("template");

		Message message = new Message();
		message.setAttachment(attachment);

		return message;
	}

	Recipient getRecipient(String senderId) {
		Recipient recipient = new Recipient();
		recipient.setId(senderId);
		return recipient;
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
		reply.setRecipient(getRecipient(senderId));
		reply.setMessage(message);

		String jsonReply = new Gson().toJson(reply);
		return jsonReply;
	}

	private Message getVRCards() {
		List<Element> elements = getElements();
		Payload payload = new Payload();
		payload.setElements(elements);
		payload.setTemplateType("generic");

		return getQuestion(payload);
	}

	private List<Element> getElements() {
		List<Element> elements = new ArrayList<>();

		for (TitleSubTitle image : vrImageUrls) {
			Element element = new Element();
			elements.add(element);

			List<Button> buttons = getButtons(image.title, image.url);
			element.setButtons(buttons);

			element.setTitle(image.title);
			element.setSubtitle(image.subTitle);
			element.setImageUrl(image.url);

		}
		return elements;
	}

	private List<Button> getButtons(String title, String url) {
		List<Button> buttons = new ArrayList<>();
		Button showDetail = new Button();
		buttons.add(showDetail);
		showDetail.setType("web_url");
		showDetail.setTitle("Show more detail");
		showDetail.setUrl("https://www.strollup.in/#!/newchat");

		Button book = new Button();
		buttons.add(book);
		book.setType("postback");
		book.setTitle("What's this?");
		book.setPayload(vr);

		return buttons;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!StringUtils.isEmpty(jsonString)) {
			Gson gson = new Gson();
			t = gson.fromJson(jsonString, clazz);
		}
		return t;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonReplies;
	}

	private Element buildElement(Result r) {
		Element e = new Element();
		e.setTitle(r.getName());
		if (r.getPhotos().size() > 0) {
			try {
				URLConnection conn = (new URL(PlacesAPI.getPhotoURL(r.getPhotos().get(0).getPhotoreference())))
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
		default_action.setUrl(r.getIcon());
		e.setDefault_action(default_action);
		e.setButtons(addButton(r.getPlace_id()));
		return e;
	}

	private String getDescription(Result r) {
		String res = r.getName()
				+ " is located in the vicinity of {vicinity}. Rated {rating} and has {reviews} reviews. ";
		Random rand = new Random();
		res = res.replace("{vicinity}", r.getVicinity());
		res = res.replace("{rating}", "" + (r.getRating() == 0.0 ? (rand.nextInt(24) + 26) / 10.0 : r.getRating()));
		res = res.replace("{reviews}", "" + rand.nextInt(100));
		res = res.replace("{timeSpent}", "" + (rand.nextInt(10) + 1) / 2.0);

		return res;
	}

	private String getSubtitle(Result r) {

		String res = "People generally spend around {timeSpent} hour here.";
		res = res.replace("{timeSpent}", "" + (int) (new Random().nextInt(10) + 2) / 2.0);
		return res;
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
				userRecommendations.remove(senderId);
				// build url route map.
				if (removeIndex)
					results.remove(foundAt);

				List<Element> elements = new ArrayList<>();

				for (Result r : nearbyRes.getResults()) {
					elements.add(buildElement(r));
				}

				Message attachMsg = new Message();
				attachMsg.setAttachment(new Attachment());
				attachMsg.getAttachment().setType(Constants.Types.template.name());
				attachMsg.getAttachment().setPayload(new Payload());
				attachMsg.getAttachment().getPayload().setTemplateType(Constants.Types.list.name());
				attachMsg.getAttachment().getPayload().setElements(elements);
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

	private List<String> sendNextItemToBot(String senderId, int index,
			ConcurrentHashMap<String, NearbyResponse> userRecommendations) {
		List<String> jsonReplies = new ArrayList<String>();

		NearbyResponse nearbyResponse = userRecommendations.get(senderId);
		List<Result> res = nearbyResponse.getResults();
		List<Element> elems = new ArrayList<Element>();
		Message textMsg = new Message();

		Result next = res.get(index);
		elems.add(buildElement(next));
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
