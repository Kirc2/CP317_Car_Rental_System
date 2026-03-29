package main.java.com.carrental.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.util.HTTPUtils;
import main.java.com.carrental.util.JSONUtil;
import main.java.com.carrental.util.ModelUtil;

public class StaffEndpoints {

    /**
     * Add a new vehicle.
     * Expects JSON: { "licensePlate": "...", "make": "...", "model": "...", "year": 2020, "dailyRate": 49.99, "type": "SEDAN" }
     */
    public static class AddVehicleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = JSONUtil.readBody(exchange);
            try {
                String licensePlate = JSONUtil.extractField(body, "licensePlate");
                String make = JSONUtil.extractField(body, "make");
                String model = JSONUtil.extractField(body, "model");
                String yearStr = JSONUtil.extractField(body, "year");
                String dailyRateStr = JSONUtil.extractField(body, "dailyRate");
                String typeStr = JSONUtil.extractField(body, "type");

                if (licensePlate == null || make == null || model == null || yearStr == null || dailyRateStr == null || typeStr == null) {
                    JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON, "{\"error\":\"Missing fields\"}");
                    return;
                }

                int year = Integer.parseInt(yearStr);
                double dailyRate = Double.parseDouble(dailyRateStr);
                Vehicle.VehicleType type = ModelUtil.getTypeFromString(typeStr.toUpperCase());

                Vehicle vehicle = new Vehicle();
                vehicle.setLicensePlate(licensePlate);
                vehicle.setMake(make);
                vehicle.setModel(model);
                vehicle.setYear(year);
                vehicle.setDailyRate(dailyRate);
                vehicle.setType(type);
                vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);

                VehicleDAO.insertRecord(vehicle);
                JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, "{\"message\":\"Vehicle added successfully\"}");
            } catch (Exception e) {
                e.printStackTrace();
                JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, "{\"error\":\"Failed to add vehicle\"}");
            }
        }
    }

    /**
     * Update vehicle status.
     * Expects JSON: { "id": "vehicleId", "status": "AVAILABLE" }
     */
    public static class UpdateVehicleStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = JSONUtil.readBody(exchange);
            try {
                String id = JSONUtil.extractField(body, "id");
                String statusStr = JSONUtil.extractField(body, "status");
                if (id == null || statusStr == null) {
                    JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON, "{\"error\":\"Missing fields\"}");
                    return;
                }
                Vehicle.VehicleStatus status = ModelUtil.getVehicleStatusFromString(statusStr);
                if (status == null) {
                    JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON, "{\"error\":\"Invalid status\"}");
                    return;
                }
                boolean success = new VehicleDAO().updateStatus(id, status);
                if (success) {
                    JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, "{\"message\":\"Status updated\"}");
                } else {
                    JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, "{\"error\":\"Update failed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, "{\"error\":\"Server error\"}");
            }
        }
    }

    /**
     * Search vehicles by ID or type.
     * Query params: ?id=... or ?type=...
     */
    public static class SearchVehiclesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
                return;
            }
            Map<String, String> params = HTTPUtils.parseQueryString(exchange.getRequestURI().getQuery());
            String id = params.get("id");
            String typeStr = params.get("type");

            List<Vehicle> vehicles = new ArrayList<>();
            if (id != null && !id.isEmpty()) {
                Vehicle v = VehicleDAO.findByID(id);
                if (v != null) vehicles.add(v);
            } else if (typeStr != null && !typeStr.isEmpty()) {
                Vehicle.VehicleType type = ModelUtil.getTypeFromString(typeStr.toUpperCase());
                if (type != null) {
                    vehicles = new VehicleDAO().findByType(type);
                }
            } else {
                // If no filter, return all vehicles
                vehicles = new VehicleDAO().findAllVehicles();
            }

            String json = JSONUtil.VehiclesToJson(vehicles);
            JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, json);
        }
    }

    /**
     * List all rentals with customer and vehicle info.
     */
    public static class ListRentalsHandler implements HttpHandler {
        private RentalDAO rentalDAO = new RentalDAO();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
                return;
            }
            try {

                List<Rental> rentals = rentalDAO.findAllRentals();
                List<Map<String, Object>> list = new ArrayList<>();
                for (Rental r : rentals) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getRentalID());
                    map.put("customerName", r.getCustomer() != null ? r.getCustomer().getCustomerName() : "Unknown");
                    map.put("vehicleInfo", r.getVehicle() != null ? r.getVehicle().getMake() + " " + r.getVehicle().getModel() : "Unknown");
                    map.put("startDate", r.getPickupDate().toString());
                    map.put("endDate", r.getPlannedReturnDate().toString());
                    map.put("totalCost", r.getTotalCost());
                    map.put("status", r.getStatus().name());
                    list.add(map);
                }
                String json = JSONUtil.listMapToJson(list);
                JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, json);
            } catch (Exception e) {
                e.printStackTrace();
                JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, "{\"error\":\"Failed to fetch rentals\"}");
            }
        }
    }

    /**
     * Cancel a rental.
     * Expects JSON: { "id": "rentalId" }
     */
    public static class CancelRentalHandler implements HttpHandler {
        private RentalDAO rentalDAO = new RentalDAO();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                JSONUtil.sendResponse(exchange, HTTPUtils.METHOD_NOT_ALLOWED, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = JSONUtil.readBody(exchange);
            try {
                String id = JSONUtil.extractField(body, "id");
                if (id == null || id.isEmpty()) {
                    JSONUtil.sendResponse(exchange, HTTPUtils.INVALID_JSON, "{\"error\":\"Missing rental ID\"}");
                    return;
                }
                boolean success = rentalDAO.cancelRental(id);
                if (success) {
                    JSONUtil.sendResponse(exchange, HTTPUtils.SUCCESSFUL_RESPONSE, "{\"message\":\"Rental cancelled\"}");
                } else {
                    JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, "{\"error\":\"Cancellation failed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JSONUtil.sendResponse(exchange, HTTPUtils.UNEXPECTED_SERVER_ERROR, "{\"error\":\"Server error\"}");
            }
        }
    }
	
}
