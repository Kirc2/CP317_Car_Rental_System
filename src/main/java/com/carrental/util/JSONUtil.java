package main.java.com.carrental.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import main.java.com.carrental.model.Customer;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;

public class JSONUtil {
	
    /**
     * Helper method to turn any regular string into a send ready jsonify text
     * @param the text you would like to jsonify
     * @return the string jsonified
     */
    public static String jsonifyString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"');

        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");   // optional, but safe
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Helper method to 'decrypt' the text body sent by the web server client
     * @param HttpExchange usally found in classes
     * @return String to extract client information from
     */
    public static String readBody(HttpExchange exchange) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("GET "+ sb.toString());
        } catch (IOException e) {
			e.printStackTrace();
		}
        return sb.toString();
    }
    /**
     * Sends a response to the web server client, self explanintory
     * @param HttpExchange usally found in classes
     * @param StatusCode so the client knows what happend in its send process 
     * @param String the reponse you want to Send
     * @throws IOException
     */
    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            System.out.println("POST "+ response.getBytes(StandardCharsets.UTF_8));
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Extracts a field from a json string
     * @param String entire json string
     * @param fieldName
     * @return the information taken from said field
     */
    public static String extractField(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Parses a Customer instance from the json sent by the web server client
     * @param String json that was sent
     * @return new instance of a Customer
     */
    public static Customer parseCustomerFromJson(String json) {
        String name = extractField(json, "name");
        String email = extractField(json, "email");
        String password = extractField(json, "password");

        if (name == null || email == null || password == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setCustomerName(name);
        customer.setEmail(email);
        String hashedpass = SecurePasswordHasher.hashPassword(password);
        customer.setPassword(hashedpass);
        return customer;
    }

    /**
     * return a customer to its json string format
     * @param Customer to translate
     * @return String of json
     */
    public static String customerToJson(Customer c) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"licenseNumber\":\"%s\"}",
            c.getCustomerID(), escapeJson(c.getCustomerName()), escapeJson(c.getEmail()),
            escapeJson(c.getPhone()), escapeJson(c.getLicenseNumber())
        );
    }

    /**
     * escapes special characters in a string
     * so that it can be safely embedded inside a JSON string 
     * @param String to escape
     * @return escaped string
     */
    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
    public static String RentalsToJson(List<Rental> rentals) {
    	 StringBuilder json = new StringBuilder("[");
         for (int i = 0; i < rentals.size(); i++) {
             Rental r = rentals.get(i);
             json.append("{")
                 .append("\"id\":").append(r.getRentalID()).append(",")
                 .append("\"carName\":\"").append(JSONUtil.escapeJson(r.getVehicle().getMake() + " " + r.getVehicle().getModel() + " (" + r.getVehicle().getYear() + ")")).append("\",")
                 .append("\"start\":\"").append(r.getPickupDate().toLocalDate()).append("\",")
                 .append("\"end\":\"").append(r.getPlannedReturnDate().toLocalDate()).append("\",")
                 .append("\"total\":\"").append(r.getTotalCost()).append("\"")
                 .append("}");
             if (i < rentals.size() - 1) json.append(",");
         }
         json.append("]");
         return json.toString();
    }
    
    public static String VehiclesToJson(List<Vehicle> vehicles) {
        if (vehicles == null || vehicles.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v = vehicles.get(i);
            // Build a JSON object for this vehicle
            json.append("{");
            json.append("\"id\":").append(v.getId()).append(",");
            json.append("\"type\":\"").append(escapeJson(v.getType().name())).append("\",");
            json.append("\"make\":\"").append(escapeJson(v.getMake())).append("\",");
            json.append("\"model\":\"").append(escapeJson(v.getModel())).append("\",");
            json.append("\"year\":").append(v.getYear()).append(",");
            json.append("\"price\":").append(v.getDailyRate()).append(",");
            json.append("\"status\":\"").append(escapeJson(v.getStatus().name())).append("\"");
            json.append("}");

            if (i < vehicles.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

}
