package gator.fb.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import gator.fb.contract.Postback;
import gator.fb.utils.FbChatHelper;
import gator.google.contract.NearbyResponse;
import gator.utils.Constants;

/**
 * @author takirala
 */
public class WebHookServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/************* FB Chat Bot variables *************************/
	private static String PAGE_TOKEN;
	private static String VERIFY_TOKEN;
	private static String FB_MSG_URL;
	private static String CARET_URL;
	private static String PROFILE_URL;

	private static final HttpClient client = HttpClientBuilder.create().build();
	private static final HttpPost httppost = new HttpPost(FB_MSG_URL);
	private static final FbChatHelper helper = new FbChatHelper();
	/*************************************************************/

	private static ConcurrentHashMap<String, NearbyResponse> userRecommendations = new ConcurrentHashMap<>();

	@Override
	public void init() throws ServletException {
		httppost.setHeader("Content-Type", "application/json");
		System.out.println("webhook servlet created!!");
		Properties prop = new Properties();
		try {
			String fileName = getServletContext().getInitParameter("propertyFilePath");
			System.out.println("Loading file : " + fileName);
			prop.load(new FileInputStream(fileName));
			initializeParams(prop);

			System.out.println(prop.size());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		setCaret();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Get method is used by FB messenger to verify the webhook
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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

	public static String getPROFILE_URL(String senderId) {
		return PROFILE_URL.replace("{SENDER_ID}", senderId);
	}

	private void initializeParams(Properties prop) {
		PAGE_TOKEN = prop.getProperty(PAGE_TOKEN);
		VERIFY_TOKEN = prop.getProperty(VERIFY_TOKEN);
		FB_MSG_URL = prop.getProperty(FB_MSG_URL) + PAGE_TOKEN;
		CARET_URL = prop.getProperty(CARET_URL) + PAGE_TOKEN;
		PROFILE_URL = prop.getProperty(PROFILE_URL) + PAGE_TOKEN;
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
		for (Messaging event : messagings) {

			try {
				String senderID = event.getSender().getId();

				Message msgObj = event.getMessage();
				Postback postback = event.getPostback();

				if (msgObj == null && postback == null) {
					System.err.println("No msg or postback. return");
					return;
				}

				if (msgObj != null && msgObj.getText() != null && msgObj.getText().equals("clear")) {
					userRecommendations.remove(senderID);
					break;
				}

				if (postback == null && msgObj.getAttachments() == null) {
					handleWelcomeMessage(senderID);
					break;
				}

				if (postback == null && msgObj.getAttachments() != null) {

					// we now have the location.
					// send places one by one.
					// Attachment is there. ignore the text.
					// Render only location as of now..
					for (Attachment attach : msgObj.getAttachments()) {
						if (attach.getType().equals(Constants.Types.location.name())) {
							double latitude = attach.getPayload().getCoordinates().getLatitude();
							double longitude = attach.getPayload().getCoordinates().getLongitude();
							setTypingOnStatus(senderID);
							setGooglePlacesResponse(senderID, latitude, longitude);
							break;
						}
					}
				} else if (postback != null) {
					String payload = postback.getPayload();

					if (payload.startsWith("LoadMore")) {
						processFinalRoute(senderID);
						userRecommendations.remove(senderID);
					} else
						processUserRecommendations(senderID, postback.getPayload());
				} else {
					System.err.println("Unknown Case");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		response.setStatus(HttpServletResponse.SC_OK);
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

	private void setTypingOnStatus(String senderID) throws Exception {
		String typing_on = helper.getTypingStatus(senderID);
		HttpEntity entity = new ByteArrayEntity(((String) typing_on).getBytes("UTF-8"));
		httppost.setEntity(entity);
		HttpResponse response = client.execute(httppost);
		String result = EntityUtils.toString(response.getEntity());
		if (Constants.isDebugEnabled)
			System.out.println(result);
	}

	private void setGooglePlacesResponse(String senderId, double latitude, double longitude) throws Exception {

		List<String> res = helper.getGooglePlacesRes(senderId, latitude, longitude, userRecommendations);

		for (String r : res) {

			HttpEntity entity = new ByteArrayEntity(((String) r).getBytes("UTF-8"));
			httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);
			String result = EntityUtils.toString(response.getEntity());
			if (Constants.isDebugEnabled)
				System.out.println(result);
		}
	}

	private void processUserRecommendations(String senderId, String payloadMessage) throws Exception {
		List<String> res = helper.processUserRecommendations(senderId, payloadMessage, userRecommendations);
		for (String r : res) {

			HttpEntity entity = new ByteArrayEntity(((String) r).getBytes("UTF-8"));
			httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);
			String result = EntityUtils.toString(response.getEntity());
			if (Constants.isDebugEnabled)
				System.out.println(result);
		}
	}

	private void processFinalRoute(String senderID) throws Exception {
		List<String> res = helper.processFinalRoute(senderID, userRecommendations);
		for (String r : res) {

			HttpEntity entity = new ByteArrayEntity(((String) r).getBytes("UTF-8"));
			httppost.setEntity(entity);
			HttpResponse response = client.execute(httppost);
			String result = EntityUtils.toString(response.getEntity());
			if (Constants.isDebugEnabled)
				System.out.println(result);
		}
	}

	private void setCaret() {

		String body = "{\"setting_type\" : \"call_to_actions\",\"thread_state\" : \"existing_thread\",\"call_to_actions\":[{\"type\":\"postback\",\"title\":\"Help\", \"payload\":\"DEVELOPER_DEFINED_PAYLOAD_FOR_HELP\"},{\"type\":\"postback\",\"title\":\"Start a New Order\",\"payload\":\"DEVELOPER_DEFINED_PAYLOAD_FOR_START_ORDER\"},{\"type\":\"web_url\",\"title\":\"Checkout\",\"url\":\"http://petersapparel.parseapp.com/checkout\",\"webview_height_ratio\": \"full\",\"messenger_extensions\": true},{\"type\":\"web_url\",\"title\":\"View Website\",\"url\":\"http://petersapparel.parseapp.com/\"}]}";
		try {
			HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
			HttpPost httppost = new HttpPost(CARET_URL);
			httppost.setEntity(entity);
			client.execute(httppost);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void destroy() {
		System.out.println("webhook Servlet Destroyed");
	}

}
