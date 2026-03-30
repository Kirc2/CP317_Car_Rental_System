package main.java.com.carrental.gui;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import main.java.com.carrental.service.CustomerService;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;

/**
 * Handles profile editing endpoints.
 */
public class ProfileEndpoints {

    public static class UpdateEmailHandler implements HttpHandler {
        private CustomerService customerService = new CustomerService();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only allow POST requests
            if (!"POST".equals(exchange.getRequestMethod())) {
                JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED,
                        "{\"error\":\"Method not allowed\"}");
                return;
            }

            // Read request body and extract fields
            String body = JSONUtil.readBody(exchange);
            String oldEmail = JSONUtil.extractField(body, "oldEmail");
            String newEmail = JSONUtil.extractField(body, "newEmail");

            if (oldEmail == null || newEmail == null) {
                JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON,
                        "{\"error\":\"Missing email fields\"}");
                return;
            }

            // Perform the update
            boolean success = customerService.updateEmail(oldEmail, newEmail);

            if (success) {
                JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE,
                        "{\"message\":\"Email updated successfully\"}");
            } else {
                JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON,
                        "{\"error\":\"Email update failed. Check old email or try again.\"}");
            }
        }
    }
}