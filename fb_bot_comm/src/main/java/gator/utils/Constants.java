package gator.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

	public static final List<String> placeTypes = Arrays.asList("amusement_park", "aquarium", "art_gallery", "bar",
			"bowling_alley", "casino", "library", "movie_theater", "museum", "night_club", "restaurant",
			"shopping_mall", "spa", "stadium", "university", "zoo");

	static ArrayList<String> commands = new ArrayList<>();
	public static boolean isDebugEnabled = true;

	static {
		commands.add("YES");
		commands.add("NO");
	}

	public static final String welcomeMessage = "Howdy {0}! Gator Knuckle Reporting. Chime in your location to make the most out of this trip";

	public static enum Types {
		location, postback, template, list, generic, web_url
	}

	public static enum Heights {
		tall, full
	}

	public static boolean isCommand(String cmd) {
		return commands.contains(cmd);
	}

}
