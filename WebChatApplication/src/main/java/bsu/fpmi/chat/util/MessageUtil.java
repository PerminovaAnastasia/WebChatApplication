package bsu.fpmi.chat.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class MessageUtil {
	public static final String TOKEN = "token";
	private static final String ID = "id";
	public static final String MESSAGES = "messages";
	private static final String USERNAME = "username";
	private static final String TEXT = "text";
	private static final String EDIT = "edit";
	private static final String DELETE = "delete";
	private static final String TIME = "time";

	private MessageUtil() {
	}

	public static String getToken(int index) {
		return String.valueOf(index);
	}

	public static int getIndex(String token) {
		return (Integer.valueOf(token));
	}

	public static JSONObject stringToJson(String data) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(data.trim());
	}
}
