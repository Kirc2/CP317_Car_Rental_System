package main.java.com.carrental.gui;

import java.io.IOException;
import java.util.List;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.service.VehicleService;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;

/**
 * Class that handles the vehicle search page will return JSON depending on the filters given by the page.
 * Endpoint is at Rentals.html
 */
public class VehicleSearchEndpoints implements HttpHandler{

	VehicleService vecService = new VehicleService();
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
	    if (!"POST".equals(exchange.getRequestMethod())) {
	        JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
	        return;
	    }
	    String body = JSONUtil.readBody(exchange);
	    try {
	        List<Vehicle> vehicles = vecService.getVehiclesFromFilters(body);
	        String jsonResponse = JSONUtil.VehiclesToJson(vehicles);
	        JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, jsonResponse);
	    } catch (Exception e) {
	        e.printStackTrace();
	        JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, 
	                              "{\"error\":\"Unexpected error occurred\"}");
	    }		
	}
}