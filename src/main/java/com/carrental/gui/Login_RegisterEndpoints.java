package main.java.com.carrental.gui;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.carrental.model.Customer;
import main.java.com.carrental.service.CustomerService;
import main.java.com.carrental.util.JSONUtil;

public class Login_RegisterEndpoints {
	
	private static CustomerService customerService = new CustomerService();
	
	/**
	 * Class to handle what happens when the user wants to register, calls methods from
	 * CustomerDAO in order to insert records in the database
	 */
	  public static class RegisterHandler implements HttpHandler {
	        @Override
	        public void handle(HttpExchange exchange) throws IOException {
	            if (!"POST".equals(exchange.getRequestMethod())) {
	            	JSONUtil.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
	                return;
	            }
	            String body = JSONUtil.readBody(exchange);
	            Customer customer = JSONUtil.parseCustomerFromJson(body);
	            if (customer == null) {
	            	JSONUtil.sendResponse(exchange, 400, "{\"error\":\"Invalid JSON\"}");
	                return;
	            }
	            try { 	
	            	customerService.Register(customer);
	            	String jsonResponse = JSONUtil.customerToJson(customer);
	                JSONUtil.sendResponse(exchange, 201, jsonResponse);
	            } catch (Exception e) {
	            	JSONUtil.sendResponse(exchange, 400, "{\"error\":\"" + JSONUtil.escapeJson(e.getMessage()) + "\"}");
	            }
	        }
	    }

	    /**
	     * Class to handle a client logging in, will pull records from the MySQL database
	     * in order to verify the client
	     */
	    public static class LoginHandler implements HttpHandler {
	        @Override
	        public void handle(HttpExchange exchange) throws IOException {
	            if (!"POST".equals(exchange.getRequestMethod())) {
	                JSONUtil.sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
	                return;
	            }
	            String body = JSONUtil.readBody(exchange);
	            String email = JSONUtil.extractField(body, "email");
	            String password = JSONUtil.extractField(body, "password");
	            if (email == null || password == null) {
	            	JSONUtil.sendResponse(exchange, 400, "{\"error\":\"Missing email or password\"}");
	                return;
	            }
	            try {            	
	                Customer customer = customerService.Login(email, password);
	                JSONUtil.sendResponse(exchange, 200, JSONUtil.jsonifyString("Login Successful"));
	            } catch (Exception e) {
	            	JSONUtil.sendResponse(exchange, 401, "{\"error\":\"" + JSONUtil.escapeJson(e.getMessage()) + "\"}");
	            }
	        }
	    }
}
