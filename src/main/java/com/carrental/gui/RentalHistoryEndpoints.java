package main.java.com.carrental.gui;

import java.io.IOException;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.service.PersistentData;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;

/**
 * Endpoints for rental history view. Gets the customers rental history based on their id
 * 
 */
public class RentalHistoryEndpoints implements HttpHandler{

    private RentalDAO rentalDAO = new RentalDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	System.out.println("HistoryHandler: received " + exchange.getRequestMethod() + " request for " + exchange.getRequestURI());
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
