package main.java.com.carrental.gui;

import java.io.IOException;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;

public class RentalHistoryEndpoints implements HttpHandler{

    private RentalDAO rentalDAO = new RentalDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
            return;
        }


        String query = exchange.getRequestURI().getQuery();
        int customerId = -1;
        if (query != null && query.startsWith("customerId=")) {
            try {
                customerId = Integer.parseInt(query.substring(11));
            } catch (NumberFormatException e) {
            }
        }

        if (customerId == -1) {
            JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, "{\"error\":\"Missing or invalid customerId\"}");
            return;
        }
        List<Rental> rentals = rentalDAO.getRentalsByCustomer(customerId);
        String rental = JSONUtil.RentalsToJson(rentals);

        JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, rental);
    }

}
