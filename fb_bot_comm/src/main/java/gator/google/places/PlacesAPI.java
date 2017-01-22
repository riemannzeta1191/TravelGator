package gator.google.places;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.http.HttpEntity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import gator.google.contract.Location;
import gator.google.contract.Result;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gator.google.contract.NearbyResponse;
import gator.google.contract.Result;

public class PlacesAPI {

	static final String API_KEY = "AIzaSyCXWBHckGfNWwlrymhKdU5VuPkMWaVwbmg";

	static final String Direction_KEY = "AIzaSyC4aXq3pZnizKtXqU9eC1_z1KHprAAPjFc";
	static final String NEARBY_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	static final String Direction_URL = "https://maps.googleapis.com/maps/api/directions/json?";
	static final String Map_URL = "https://www.google.com/maps/dir";

	static final String API_KEY_PHOTO = "AIzaSyB7_drW4kjos6Y-OVk8jLs7h6CwkprrkV4";

	static final String NEARBY_URL1 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	static final String URL_PHOTO = "https://maps.googleapis.com/maps/api/place/photo?key="
			+ API_KEY_PHOTO + "&photoreference={photo_reference}&maxwidth=400";

	private static final HttpClient client = HttpClientBuilder.create().build();
	private static final HttpGet httpGet = new HttpGet(NEARBY_URL + API_KEY);
	private static final String[] places = {"amusement_park", "aquarium", "art_gallery",
											"bar", "beauty_salon", "bowling_alley", "cafe",
											"casino", "church", "clothing_store", "convenience_store",
											"furniture_store", "hindu_temple", "library", "movie_theater",
											"museum", "night_club", "park", "restaurant", "shopping_mall",
											"spa", "stadium", "university", "zoo"
											};
			
			

	private static double lat = 0;
	private static double lng = 0;

	public static NearbyResponse getNearbyPlaces(double latitude,
			double longitude) throws Exception {
		URIBuilder builder = new URIBuilder(NEARBY_URL);
		builder.addParameter("key", API_KEY);
		lat = latitude;
		lng = longitude;
		builder.addParameter("location", latitude + "," + longitude);
		builder.addParameter("radius", "1000");
		httpGet.setURI(builder.build());
		HttpResponse response = client.execute(httpGet);

		String body = EntityUtils.toString(response.getEntity(), "UTF-8");

		NearbyResponse nearbyResponse = new Gson().fromJson(body,
				NearbyResponse.class);

		System.out.println(nearbyResponse.getResults().size());

		getDirections(nearbyResponse);

		// System.out.println(nearbyResponse.getResults().size());
		return null;

	}

	// My func
	public static void getDirections(NearbyResponse nearbyResponse)
			throws Exception {

		URIBuilder builder = new URIBuilder(Direction_URL);
		builder.addParameter("key", Direction_KEY);
		builder.addParameter("origin", lat + "," + lng);
		builder.addParameter("destination", lat + "," + lng);
		List<Result> places = nearbyResponse.getResults();

		String waypoints = "";

		for (Result rs : places) {

			waypoints = waypoints + "|"
					+ rs.getGeometry().getLocation().getLat() + ","
					+ rs.getGeometry().getLocation().getLng();

		}

		waypoints = "optimize:true" + waypoints;

		builder.addParameter("waypoints", waypoints);

		httpGet.setURI(builder.build());
		System.out.println(httpGet.getURI());

		HttpResponse response = client.execute(httpGet);

		String body = EntityUtils.toString(response.getEntity(), "UTF-8");
		System.out.println(body);

		NearbyResponse directionsResponse = new Gson().fromJson(body,
				NearbyResponse.class);

		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(body).getAsJsonObject();

		JsonArray js = json.getAsJsonArray("routes");

		JsonObject Json = js.get(0).getAsJsonObject();
		// System.out.println(Json);

		String arr = Json.get("waypoint_order").toString();
		System.out.println(arr);

		String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "")
				.replaceAll("\\s", "").split(",");

		int[] results = new int[items.length];

		for (int i = 0; i < items.length; i++) {
			try {
				results[i] = Integer.parseInt(items[i]);
			} catch (NumberFormatException nfe) {
				// NOTE: write something here if you need to recover from
				// formatting errors
			}
			;
		}

		System.out.println(results[0]);

		List<Result> optimumOrder = new ArrayList<Result>(places.size());

		for (int i = 0; i < results.length; i++) {
			optimumOrder.add(places.get(results[i]));
		}

		// getDirectionMap(optimumOrder);
	}

	public static String getDirectionMap(NearbyResponse nb) throws Exception {

		List<Result> optimumOrder = nb.getResults();

		URIBuilder builder = new URIBuilder(Map_URL);
		String locations = "";
		for (int i = 0; i < optimumOrder.size(); i++) {

			locations = locations + "/"
					+ optimumOrder.get(i).getGeometry().getLocation().getLat()
					+ ","
					+ optimumOrder.get(i).getGeometry().getLocation().getLng();

		}

		locations = Map_URL + locations;
		System.out.println(locations);
		return locations;
	}

	public static String getPhotoURL(String photo_reference) {
		return URL_PHOTO.replace("{photo_reference}", photo_reference);

	}
}
