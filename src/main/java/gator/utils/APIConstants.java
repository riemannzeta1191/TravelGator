package gator.utils;

import java.util.Properties;

public class APIConstants {

	private static String API_KEY_PHOTO;
	private static String API_KEY_DIRECTION;
	private static String API_KEY_PLACES;

	private static String URL_DIRECTION;
	private static String URL_MAP;
	private static String URL_PLACE_DETAILS;
	private static String URL_RADAR_SEARCH;

	private static String URL_PHOTO;

	public static String getPhotoURL(String photo_reference) {
		return APIConstants.getURL_PHOTO().replace("{photo_reference}", photo_reference);
	}

	public static String getURL_MAP() {
		return URL_MAP;
	}

	public static void setURL_MAP(String uRL_MAP) {
		URL_MAP = uRL_MAP;
	}

	public static String getURL_DIRECTION() {
		return URL_DIRECTION;
	}

	public static void setURL_DIRECTION(String uRL_DIRECTION) {
		URL_DIRECTION = uRL_DIRECTION;
	}

	public static String getURL_PLACE_DETAILS() {
		return URL_PLACE_DETAILS;
	}

	public static void setURL_PLACE_DETAILS(String uRL_PLACE_DETAILS) {
		URL_PLACE_DETAILS = uRL_PLACE_DETAILS;
	}

	public static String getURL_RADAR_SEARCH() {
		return URL_RADAR_SEARCH;
	}

	public static void setURL_RADAR_SEARCH(String uRL_RADAR_SEARCH) {
		URL_RADAR_SEARCH = uRL_RADAR_SEARCH;
	}

	public static String getAPI_KEY_PHOTO() {
		return API_KEY_PHOTO;
	}

	public static void setAPI_KEY_PHOTO(String aPI_KEY_PHOTO) {
		API_KEY_PHOTO = aPI_KEY_PHOTO;
	}

	public static String getAPI_KEY_DIRECTION() {
		return API_KEY_DIRECTION;
	}

	public static void setAPI_KEY_DIRECTION(String aPI_KEY_DIRECTION) {
		API_KEY_DIRECTION = aPI_KEY_DIRECTION;
	}

	public static String getAPI_KEY_PLACES() {
		return API_KEY_PLACES;
	}

	public static void setAPI_KEY_PLACES(String aPI_KEY_PLACES) {
		API_KEY_PLACES = aPI_KEY_PLACES;
	}

	public static String getURL_PHOTO() {
		return URL_PHOTO;
	}

	public static void setURL_PHOTO(String uRL_PHOTO) {
		URL_PHOTO = uRL_PHOTO;
	}

	static void initializeProperties(Properties props) {
		setAPI_KEY_PHOTO(props.getProperty("API_KEY_PHOTO"));
		setAPI_KEY_DIRECTION(props.getProperty("API_KEY_DIRECTION"));
		setAPI_KEY_PLACES(props.getProperty("API_KEY_PLACES"));
		setURL_DIRECTION(props.getProperty("URL_DIRECTION"));
		setURL_MAP(props.getProperty("URL_MAP"));
		setURL_PLACE_DETAILS(props.getProperty("URL_PLACE_DETAILS"));
		setURL_RADAR_SEARCH(props.getProperty("URL_RADAR_SEARCH"));
	}

}
