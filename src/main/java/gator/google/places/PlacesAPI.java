package gator.google.places;

import java.util.ArrayList;
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
import gator.utils.APIConstants;
import gator.utils.Constants;

public class PlacesAPI {

	private static final HttpClient client = HttpClientBuilder.create().build();
	private static final HttpGet httpGet = new HttpGet();

	private static final int filterLocCount = 8;
	private static final int typeLocLimit = 2;

	public static String getDirections(NearbyResponse nearbyResponse) {
		String locations = "";
		try {
			double lat = nearbyResponse.getLatitude();
			double lng = nearbyResponse.getLongitude();

			URIBuilder builder = new URIBuilder(APIConstants.getURL_DIRECTION());
			builder.addParameter("key", APIConstants.getAPI_KEY_DIRECTION());
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

			HttpResponse response = client.execute(httpGet);

			String body = EntityUtils.toString(response.getEntity(), "UTF-8");

			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(body).getAsJsonObject();

			JsonArray js = json.getAsJsonArray("routes");

			JsonObject Json = js.get(0).getAsJsonObject();

			String arr = Json.get("waypoint_order").toString();

			String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");

			int[] results = new int[items.length];

			for (int i = 0; i < items.length; i++) {
				try {
					results[i] = Integer.parseInt(items[i]);
				} catch (NumberFormatException nfe) {

				}
			}

			// System.out.println(results[0]);

			List<Result> optimumOrder = new ArrayList<Result>(places.size());

			for (int i = 0; i < results.length; i++) {
				optimumOrder.add(places.get(results[i]));
			}

			builder = new URIBuilder(APIConstants.getURL_MAP());

			locations = locations + "/" + nearbyResponse.getLatitude() + "," + nearbyResponse.getLongitude();

			for (int i = 0; i < optimumOrder.size(); i++) {

				locations = locations + "/" + optimumOrder.get(i).getGeometry().getLocation().getLat() + ","
						+ optimumOrder.get(i).getGeometry().getLocation().getLng();

			}

			locations = locations + "/" + nearbyResponse.getLatitude() + "," + nearbyResponse.getLongitude();

			locations = APIConstants.getURL_MAP() + locations;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(locations);
		return locations;
	}

	public static NearbyResponse getNearbyPlaces(double latitude, double longitude) throws Exception {

		NearbyResponse nearbyResponse = null;
		List<Result> placeResults = new ArrayList<>();
		for (String type : Constants.placeTypes) {
			// System.out.println(type);
			URIBuilder builder = new URIBuilder(APIConstants.getURL_RADAR_SEARCH());
			builder.addParameter("key", APIConstants.getAPI_KEY_PLACES());
			builder.addParameter("location", latitude + "," + longitude);
			builder.addParameter("radius", "5000");
			builder.addParameter("type", type);
			httpGet.setURI(builder.build());
			HttpResponse response = client.execute(httpGet);

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
			URIBuilder builder = new URIBuilder(APIConstants.getURL_PLACE_DETAILS());
			builder.addParameter("key", APIConstants.getAPI_KEY_PLACES());
			builder.addParameter("placeid", placeId);
			httpGet.setURI(builder.build());
			HttpResponse response = client.execute(httpGet);
			String body = EntityUtils.toString(response.getEntity(), "UTF-8");
			PlaceResult pr = new Gson().fromJson(body, PlaceResult.class);
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
