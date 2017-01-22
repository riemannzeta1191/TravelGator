package gator.fb.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import gator.fb.contract.Attachment;
import gator.fb.contract.FbMsgRequest;
import gator.fb.contract.Message;
import gator.fb.contract.Messaging;
import gator.fb.utils.Constants;
import gator.fb.utils.FbChatHelper;
import gator.google.contract.NearbyResponse;

/**
 * 
 * @author takirala
 * 
 *
 */
public class WebHookServlet extends HttpServlet {

	private static final long serialVersionUID = -2326475169699351010L;

	/************* FB Chat Bot variables *************************/
	public static final String PAGE_TOKEN = "EAAZA2eMlDKosBAOjDjCx1UhtxjrZCbGnZCf6V8fsRxqef7DiaAyxp8DJ1UwCSHExxavb8MUuFcg9iOF0xo7c69mOrHanZAZCWpuguA7pLuNXBBwbQ8nYT2f4uNWeDkxIe292iJ5s2KeRAC1nJP4xD5d0YuWiRUqn1uviLiq49MwZDZD";
	private static final String VERIFY_TOKEN = "my_cool_funky_secret_verify_token_woah";
	private static final String FB_MSG_URL = "https://graph.facebook.com/v2.8/me/messages?access_token=" + PAGE_TOKEN;
	/*************************************************************/

	/******* for making a post call to fb messenger api **********/
	private static final HttpClient client = HttpClientBuilder.create().build();
	private static final HttpPost httppost = new HttpPost(FB_MSG_URL);
	private static final FbChatHelper helper = new FbChatHelper();

	private static ConcurrentHashMap<String, String> userState = new ConcurrentHashMap<>();

	/*************************************************************/

	final String caretURL = "https://graph.facebook.com/v2.6/me/thread_settings?access_token=" + PAGE_TOKEN;

	/*************************************************************/

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * get method is used by fb messenger to verify the webhook
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String queryString = request.getQueryString();
		String msg = "Error, wrong token";

		if (queryString != null) {
			String verifyToken = request.getParameter("hub.verify_token");
			String challenge = request.getParameter("hub.challenge");
			// String mode = request.getParameter("hub.mode");

			if (StringUtils.equals(VERIFY_TOKEN, verifyToken) && !StringUtils.isEmpty(challenge)) {
				msg = challenge;
			} else {
				msg = "";
			}
		} else {
			System.out.println("Exception no verify token found in querystring:" + queryString);
		}

		response.getWriter().write(msg);
		response.getWriter().flush();
		response.getWriter().close();
		response.setStatus(HttpServletResponse.SC_OK);
		return;
	}

	static final class UserState {
		final static String welcome_sent = "welcome_sent";
		final static String location_received = "location_received";
		final static String processing_locations = "processing_locations";
	}

	private void processRequest(HttpServletRequest httpRequest, HttpServletResponse response)
			throws IOException, ServletException {
		/**
		 * store the request body in string buffer
		 */
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = httpRequest.getReader();
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("----->" + sb.toString());

		/**
		 * convert the string request body in java object
		 */
		FbMsgRequest fbMsgRequest = new Gson().fromJson(sb.toString(), FbMsgRequest.class);
		if (fbMsgRequest == null) {
			System.out.println("fbMsgRequest was null");
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		List<Messaging> messagings = fbMsgRequest.getEntry().get(0).getMessaging();
		System.out.println(messagings);
		System.out.println(messagings.size());

		forLoop: for (Messaging event : messagings) {

			try {
				String senderID = event.getSender().getId();

				String prevState = userState.get(senderID);
				Message msgObj = event.getMessage();

				if (prevState == null || msgObj == null || msgObj.getText() == null) {
					handleWelcomeMessage(senderID);
					userState.put(senderID, UserState.welcome_sent);
					break forLoop;
				}

				if (msgObj != null && msgObj.getText().equals("clear")) {
					userState.remove(senderID);
					helper.clearUserSenderID(senderID);
					break forLoop;
				}

				switch (prevState) {

				case UserState.welcome_sent:
					// we got the location.
					// send places one by one.
					if (msgObj.getAttachments() != null) {

						// Attachment is there. ignore the text ?
						// Render only location as of now..
						for (Attachment attach : msgObj.getAttachments()) {
							if (attach.getType().equals(Constants.Types.location.name())) {
								double latitude = attach.getPayload().getCoordinates().getLatitude();
								double longitude = attach.getPayload().getCoordinates().getLongitude();
								setGooglePlacesResponse(senderID, latitude, longitude);
								userState.put(senderID, UserState.processing_locations);
								break forLoop;
							}
						}
					}
					break;

				case UserState.processing_locations:
					System.out.println("------->" + msgObj.getText());
					processUserRecommendations(senderID, msgObj.getText());
					break;
				default:
					break;
				}

				System.out.println(msgObj);
				if (msgObj == null || msgObj.getText() == null || !Constants.isCommand(msgObj.getText())) {
					// welcome message.

					continue;
				} else {
					System.out.println("Else case !!");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		response.setStatus(HttpServletResponse.SC_OK);
	}

	private void processUserRecommendations(String senderId, String string) {
		List<String> res;

	}

	private void setGooglePlacesResponse(String senderId, double latitude, double longitude) throws Exception {

		List<String> res = helper.getGooglePlacesRes(senderId, latitude, longitude);

		for (String r : res) {

			HttpEntity entity = new ByteArrayEntity(((String) r).getBytes("UTF-8"));
			httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);
			String result = EntityUtils.toString(response.getEntity());
			if (Constants.isDebugEnabled)
				System.out.println(result);
		}
	}

	private void handleWelcomeMessage(String senderId) throws Exception {
		System.out.println("Welcome : " + senderId);
		HttpEntity entity = new ByteArrayEntity(helper.getWelcomeMsg(senderId).getBytes("UTF-8"));
		httppost.setEntity(entity);
		HttpResponse response = client.execute(httppost);
		String result = EntityUtils.toString(response.getEntity());

		if (Constants.isDebugEnabled)
			System.out.println(result);
	}

	/**
	 * get the text given by senderId and check if it's a postback (button
	 * click) or a direct message by senderId and reply accordingly
	 * 
	 * @param senderId
	 * @param text
	 * @param isPostBack
	 */
	private void sendTextMessage(String senderId, String text, boolean isPostBack) throws Exception {

		List<String> jsonReplies = null;
		if (isPostBack) {
			jsonReplies = helper.getPostBackReplies(senderId, text);
		} else {
			jsonReplies = helper.getReplies(senderId, text);
		}

		for (String jsonReply : jsonReplies) {
			HttpEntity entity = new ByteArrayEntity(jsonReply.getBytes("UTF-8"));
			httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);
			String result = EntityUtils.toString(response.getEntity());
			System.out.println(result);
		}
	}

	@Override
	public void destroy() {
		System.out.println("webhook Servlet Destroyed");
	}

	@Override
	public void init() throws ServletException {
		httppost.setHeader("Content-Type", "application/json");
		System.out.println("webhook servlet created!!");
		setCaret();
	}

	public void setCaret() {

	}

}
