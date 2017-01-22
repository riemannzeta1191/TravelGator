package gator.google.places;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import gator.google.contract.NearbyResponse;

public class PlacesAPI {

	static final String API_KEY = "AIzaSyCXWBHckGfNWwlrymhKdU5VuPkMWaVwbmg";
	static final String API_KEY_PHOTO = "AIzaSyB7_drW4kjos6Y-OVk8jLs7h6CwkprrkV4";

	static final String NEARBY_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	static final String URL_PHOTO = "https://maps.googleapis.com/maps/api/place/photo?key=" + API_KEY_PHOTO
			+ "&photoreference={photo_reference}&maxwidth=400";

	private static final HttpClient client = HttpClientBuilder.create().build();
	private static final HttpGet httpGet = new HttpGet(NEARBY_URL + API_KEY);

	public static NearbyResponse getNearbyPlaces(double latitude, double longitude) throws Exception {
		URIBuilder builder = new URIBuilder(NEARBY_URL);
		builder.addParameter("key", API_KEY);
		builder.addParameter("location", latitude + "," + longitude);
		builder.addParameter("radius", "1000");
		httpGet.setURI(builder.build());
		HttpResponse response = client.execute(httpGet);

		System.out.println(httpGet.getURI());

		String body = EntityUtils.toString(response.getEntity(), "UTF-8");
		System.out.println(body);
		NearbyResponse nearbyResponse = new Gson().fromJson(body, NearbyResponse.class);
		System.out.println(nearbyResponse.getResults().size());
		return nearbyResponse;
	}

	public static String getPhotoURL(String photo_reference) {
		return URL_PHOTO.replace("{photo_reference}", photo_reference);
	}
}
