package main.java.com.carrental.gui;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.service.VehicleService;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;

public class ReservationHandler {

	public static class GETSVehicleInfo implements HttpHandler{
		VehicleService vecService = new VehicleService();
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			 if (!"GET".equals(exchange.getRequestMethod())) {
		            JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED,"{\"error\":\"Method not allowed\"}");
		            return;
		        }
	
		        URI uri = exchange.getRequestURI();
		        String query = uri.getQuery();
		        Map<String, String> params = HTTPUtils.parseQueryString(query);
		        String idParam = params.get("id");
		        if (idParam == null || idParam.isEmpty()) {
		            JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON,
		                                  "{\"error\":\"Missing vehicle ID\"}");
		            return;
		        }
		        try {
		            String vehicleId = idParam;
		            Vehicle vehicle = VehicleDAO.findByID(vehicleId);
		            if (vehicle == null) {
		                JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR,
		                                      "{\"error\":\"Vehicle not found\"}");
		                return;
		            }
		            String json = JSONUtil.VehicleToJson(vehicle);
		            JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, json);
		        } catch (NumberFormatException e) {
		            JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON,
		                                  "{\"error\":\"Invalid vehicle ID\"}");
		        } catch (Exception e) {
		            e.printStackTrace();
		            JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR,
		                                  "{\"error\":\"Unexpected error\"}");
		        }		
		}
	}
	
	public static class Reserve implements HttpHandler{

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"POST".equals(exchange.getRequestMethod())) {
	            JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED,"{\"error\":\"Method not allowed\"}");
	            return;
	        }
			
		}
		
	}

}
