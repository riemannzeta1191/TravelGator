package gator.google.places;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gator.google.contract.NearbyResponse;
import gator.google.contract.PlaceResult;
import gator.google.contract.Result;

public class PlacesAPI {

	static final String API_KEY = "AIzaSyCXWBHckGfNWwlrymhKdU5VuPkMWaVwbmg";

	static final String Direction_KEY = "AIzaSyC4aXq3pZnizKtXqU9eC1_z1KHprAAPjFc";
	static final String NEARBY_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	static final String Direction_URL = "https://maps.googleapis.com/maps/api/directions/json?";
	static final String Map_URL = "https://www.google.com/maps/dir";

	static final String API_KEY_PHOTO = "AIzaSyB7_drW4kjos6Y-OVk8jLs7h6CwkprrkV4";

	static final String NEARBY_URL1 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	static final String URL_PHOTO = "https://maps.googleapis.com/maps/api/place/photo?key=" + API_KEY_PHOTO
			+ "&photoreference={photo_reference}&maxwidth=400";

	private static final HttpClient client = HttpClientBuilder.create().build();
	private static final HttpGet httpGet = new HttpGet(NEARBY_URL + API_KEY);

	static final String PLACES_API_KEY = "AIzaSyDeUu4Jo92ulHd-dH4FAhu1tCfBKjHA7KU";
	static final String PLACE_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	static final String RADAR_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/radarsearch/json?";
	private static final HttpGet placeHttpGet = new HttpGet(PLACE_DETAILS_URL);
	private static final HttpGet nearbyPlacesHttpGet = new HttpGet(RADAR_SEARCH_URL);
	private static final List<String> placeTypes = Arrays.asList("amusement_park", "aquarium", "art_gallery", "bar",
			"bowling_alley", "casino", "library", "movie_theater", "museum", "night_club", "restaurant",
			"shopping_mall", "spa", "stadium", "university", "zoo");
	private static final int filterLocCount = 8;
	private static final int typeLocLimit = 2;

	// My func
	public static String getDirections(NearbyResponse nearbyResponse) {
		String locations = "";
		try {
			double lat = nearbyResponse.getLatitude();
			double lng = nearbyResponse.getLongitude();

			URIBuilder builder = new URIBuilder(Direction_URL);
			builder.addParameter("key", Direction_KEY);
			builder.addParameter("origin", lat + "," + lng);
			builder.addParameter("destination", lat + "," + lng);
			List<Result> places = nearbyResponse.getResults();

			String waypoints = "";

			for (Result rs : places) {

				waypoints = waypoints + "|" + rs.getGeometry().getLocation().getLat() + ","
						+ rs.getGeometry().getLocation().getLng();

			}

			waypoints = "optimize:true" + waypoints;

			builder.addParameter("waypoints", waypoints);

			httpGet.setURI(builder.build());
			System.out.println(httpGet.getURI());

			HttpResponse response = client.execute(httpGet);

			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			System.out.println("###############################");
			System.out.println(body);

			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(body).getAsJsonObject();

			JsonArray js = json.getAsJsonArray("routes");

			JsonObject Json = js.get(0).getAsJsonObject();
			System.out.println("###############################");

			System.out.println(Json);

			String arr = Json.get("waypoint_order").toString();
			System.out.println("###############################");
			System.out.println(arr);

			String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");

			int[] results = new int[items.length];

			for (int i = 0; i < items.length; i++) {
				try {
					results[i] = Integer.parseInt(items[i]);
				} catch (NumberFormatException nfe) {
					// NOTE: write something here if you need to recover from
					// formatting errors
				}
			}

			// System.out.println(results[0]);

			List<Result> optimumOrder = new ArrayList<Result>(places.size());

			for (int i = 0; i < results.length; i++) {
				optimumOrder.add(places.get(results[i]));
			}

			// optimumOrder = nearbyResponse.getResults();

			builder = new URIBuilder(Map_URL);

			locations = locations + "/" + nearbyResponse.getLatitude() + "," + nearbyResponse.getLongitude();

			for (int i = 0; i < optimumOrder.size(); i++) {

				locations = locations + "/" + optimumOrder.get(i).getGeometry().getLocation().getLat() + ","
						+ optimumOrder.get(i).getGeometry().getLocation().getLng();

			}

			locations = locations + "/" + nearbyResponse.getLatitude() + "," + nearbyResponse.getLongitude();

			locations = Map_URL + locations;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(locations);
		return locations;
	}

	public static String getPhotoURL(String photo_reference) {
		return URL_PHOTO.replace("{photo_reference}", photo_reference);
	}

	public static NearbyResponse getNearbyPlaces(double latitude, double longitude) throws Exception {

		NearbyResponse nearbyResponse = null;
		List<Result> placeResults = new ArrayList<>();
		for (String type : placeTypes) {
			// System.out.println(type);
			URIBuilder builder = new URIBuilder(RADAR_SEARCH_URL);
			builder.addParameter("key", PLACES_API_KEY);
			builder.addParameter("location", latitude + "," + longitude);
			builder.addParameter("radius", "5000");
			builder.addParameter("type", type);
			nearbyPlacesHttpGet.setURI(builder.build());
			HttpResponse response = client.execute(nearbyPlacesHttpGet);

			// System.out.println(httpGet.getURI());

			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			// System.out.println("######"+body);

			if (nearbyResponse == null) {
				nearbyResponse = new Gson().fromJson(body, NearbyResponse.class);
				nearbyResponse.setLatitude(latitude);
				nearbyResponse.setLongitude(longitude);
			}

			getHighRatedLocs(new JsonParser().parse(body).getAsJsonObject().getAsJsonArray("results"), placeResults);

			if (placeResults.size() >= filterLocCount)
				break;
		}

		nearbyResponse.setResults(placeResults);

		return nearbyResponse;
	}

	public static List<Result> getHighRatedLocs(JsonArray arrayResults, List<Result> placeResults) throws Exception {
		int thisIter = 0;
		for (JsonElement obj : arrayResults) {
			String placeId = obj.getAsJsonObject().get("place_id").getAsString();
			URIBuilder builder = new URIBuilder(PLACE_DETAILS_URL);
			builder.addParameter("key", PLACES_API_KEY);
			builder.addParameter("placeid", placeId);
			placeHttpGet.setURI(builder.build());
			HttpResponse response = client.execute(placeHttpGet);
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			// System.out.println(body);
			PlaceResult pr = new Gson().fromJson(body, PlaceResult.class);
			// System.out.println(pr + "--" + (pr.getResult()));
			// System.out.println(placeHttpGet.getURI());
			double rating = pr.getResult().getRating();
			if (rating > 3.8) {
				thisIter++;
				placeResults.add(pr.getResult());
			}
			if (thisIter >= typeLocLimit || placeResults.size() >= filterLocCount)
				break;
		}

		return placeResults;
	}
}
