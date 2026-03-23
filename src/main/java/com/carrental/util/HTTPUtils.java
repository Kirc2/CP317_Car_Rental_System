package main.java.com.carrental.util;

import java.util.HashMap;
import java.util.Map;

/*
 * Mostly used for HTTP status codes
 */
public class HTTPUtils {
	
	// ERROR CODES
	public static final int METHOD_NOT_ALLOWED = 405;
	public static final int INVALID_JSON = 400;
	public static final int EXCEPTION_ERROR_RESPONSE = 401;
	public static final int INCORRECT_CREDENTIALS = 410;
	public static final int UNEXPECTED_SERVER_ERROR = 500;
	
	// SUCCESS CODES
	public static final int SUCCESSFUL_RESPONSE = 200;
	public static final int SUCCESSFUL_REGISTRATION = 201;
	public static final int SUCCESSFUL_LOGIN = 202;
	
	
	public static Map<String, String> parseQueryString(String query) {
	    Map<String, String> params = new HashMap<>();
	    if (query != null && !query.isEmpty()) {
	        String[] pairs = query.split("&");
	        for (String pair : pairs) {
	            int idx = pair.indexOf("=");
	            if (idx > 0) {
	                String key = pair.substring(0, idx);
	                String value = pair.substring(idx + 1);
	                params.put(key, value);
	            }
	        }
	    }
	    return params;
	}
			
	
}
