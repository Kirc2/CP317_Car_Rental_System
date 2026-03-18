package main.java.com.carrental.gui;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.model.Vehicle.VehicleStatus;
import main.java.com.carrental.model.Vehicle.VehicleType;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;
import main.java.com.carrental.util.VehicleUtil;

public class VehicleSearchEndpoints implements HttpHandler{


	@Override
	public void handle(HttpExchange exchange) throws IOException {
	    if (!"POST".equals(exchange.getRequestMethod())) {
	        JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
	        return;
	    }

	    VehicleDAO vecDAO = new VehicleDAO();
	    String body = JSONUtil.readBody(exchange);

	    // Extract fields from JSON
	    String carTypeStr = JSONUtil.extractField(body, "carType");
	    String startDateStr = JSONUtil.extractField(body, "startDate");
	    String endDateStr = JSONUtil.extractField(body, "endDate");
	    String priceSort = JSONUtil.extractField(body, "priceSort");  // now a string
	    String yearStr = JSONUtil.extractField(body, "year");
	    String colour = JSONUtil.extractField(body, "colour");

	    // Convert to appropriate types (handle empty strings)
	    VehicleType type = (carTypeStr != null && !carTypeStr.isEmpty()) 
	                       ? VehicleUtil.getTypeFromString(carTypeStr) : null;
	    LocalDate startDate = (startDateStr != null && !startDateStr.isEmpty()) 
	                          ? LocalDate.parse(startDateStr) : null;
	    LocalDate endDate = (endDateStr != null && !endDateStr.isEmpty()) 
	                        ? LocalDate.parse(endDateStr) : null;
	    int year = (yearStr != null && !yearStr.isEmpty()) 
	               ? Integer.parseInt(yearStr) : 0;

	    try {
	        // Assuming your DAO method can handle nulls for optional filters
	        List<Vehicle> vehicles = vecDAO.searchVehicle(type, VehicleStatus.AVAILABLE, 
	                                                       startDate, endDate, year, 0);

	        // Convert the list to a JSON array
	        String jsonResponse = JSONUtil.VehiclesToJson(vehicles);
	        JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, jsonResponse);

	    } catch (Exception e) {
	        e.printStackTrace(); // log the error for debugging
	        JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, 
	                              "{\"error\":\"Unexpected error occurred\"}");
	    }
		
		
	}


}
