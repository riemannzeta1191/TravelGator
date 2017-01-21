package gator.fb.utils;

import java.util.ArrayList;

public class Constants {

	static ArrayList<String> commands = new ArrayList<>();
	public static boolean isDebugEnabled = true;

	static {
		commands.add("YES");
		commands.add("NO");
	}

	static final String welcomeMessage = "Hello {0}! Welcome to Gator Knuckle. Please send your location to make the most out of your trip";
	public static enum Types {
		location
	}

	public static boolean isCommand(String cmd) {
		return commands.contains(cmd);
	}

}
