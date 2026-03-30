package main.java.com.carrental.gui;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.service.RentalService;
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
		RentalService service = new RentalService();
		VehicleDAO vecDAO = new VehicleDAO();
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"POST".equals(exchange.getRequestMethod())) {
	            JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED,"{\"error\":\"Method not allowed\"}");
	            return;
	        }
			
			String body = JSONUtil.readBody(exchange);
			Rental rental = service.getRentalFromJson(body);
			boolean reserved = service.reserveVehicle(rental);
			if(reserved) {
				System.out.println("Reservation successful");
				JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, JSONUtil.jsonifyString("Vehicle has been reserved successfully"));
			} else {
				System.out.println("Could not insert rental");
				JSONUtil.sendResponse(exchange, HTTPUtils.EXCEPTION_ERROR_RESPONSE, JSONUtil.jsonifyString("Vehicle reservation was unsuccessfull, please try again later"));
			}
		}
		
	}

}