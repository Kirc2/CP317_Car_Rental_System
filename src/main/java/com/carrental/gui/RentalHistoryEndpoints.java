package main.java.com.carrental.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.service.PersistentData;
import main.java.com.carrental.service.RentalService;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;
import main.java.com.carrental.util.ModelUtil;

/**
 * Endpoints for rental history view. Gets the customers rental history based on their id
 * Endpoint is at History.html
 */
public class RentalHistoryEndpoints {

    public static class ListAllVehicles implements HttpHandler{
        private RentalDAO rentalDAO = new RentalDAO();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
                return;
            }
            try {
                List<Rental> rentals = rentalDAO.findByCustomerID(PersistentData.Persistentcustomer.getCustomerID());
                String rental = JSONUtil.RentalsToJson(rentals);
                JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, rental);
            } catch(Exception e) {
                System.out.println("Unexpected Error occured");
                JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, e.getMessage());
            }
            
        }
    }
    public static class VehicleInfo implements HttpHandler {
        	VehicleDAO vecDAO = new VehicleDAO();
        	RentalDAO rentalDAO = new RentalDAO();
        	RentalService rentalService = new RentalService();
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				 if (!"GET".equals(exchange.getRequestMethod())) {
			            JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED,
			                    "{\"error\":\"Method not allowed\"}");
			            return;
			        }
			        String query = exchange.getRequestURI().getQuery();
			        Map<String, String> params = HTTPUtils.parseQueryString(query);
			        String rentalId = params.get("id");

			        if (rentalId == null || rentalId.isEmpty()) {
			            JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON,
			                    "{\"error\":\"Missing rental ID\"}");
			            return;
			        }

			        try {
			            Rental rental = rentalDAO.findByID(rentalId);
			            if (rental == null) {
			                JSONUtil.sendResponse(exchange, HTTPUtils.EXCEPTION_ERROR_RESPONSE,
			                        "{\"error\":\"Rental not found\"}");
			                return;
			            }

			            // Fetch vehicle associated with this rental
			            Vehicle vehicle = vecDAO.findVehicleFromRentalID(rental.getVehicle().getId());
			            String carName = (vehicle != null) ?
			                    vehicle.getMake() + " " + vehicle.getModel() + " " + vehicle.getYear() :
			                    "Unknown Vehicle";

			            // Build JSON response
			            String json = String.format(
			                    "{\"id\":%s,\"carName\":\"%s\",\"start\":\"%s\",\"end\":\"%s\",\"total\":%.2f,\"status\":\"%s\"}",
			                    rental.getRentalID(),
			                    JSONUtil.escapeJson(carName),
			                    rental.getPickupDate(),
			                    rental.getPlannedReturnDate(),
			                    rental.getTotalCost(),
			                    ModelUtil.RentalStatusToString(rental.getStatus())
			            );

			            JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, json);
			        } catch (Exception e) {
			            e.printStackTrace();
			            JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR,
			                    "{\"error\":\"Unexpected error\"}");
			        }
			}
        }
    
    public static class CancelReservation implements HttpHandler {

    	RentalDAO rental = new RentalDAO();
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"POST".equals(exchange.getRequestMethod())) {
	            JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED,
	                    "{\"error\":\"Method not allowed\"}");
	            return;
	        }
			String body = JSONUtil.readBody(exchange);
			String id = JSONUtil.extractField(body, "id");
			
			boolean success = rental.cancelRental(id);
			if(success) {
	            JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE,JSONUtil.jsonifyString("Reservation has been cancelled successfully"));
			} else {
	            JSONUtil.sendResponse(exchange, HTTPUtils.EXCEPTION_ERROR_RESPONSE,"{\"error\":\"Reservation was not cancelled, contact support or try again later\"}");
			}
		}
    	
    }
    
}