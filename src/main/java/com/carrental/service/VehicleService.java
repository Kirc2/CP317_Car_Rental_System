package main.java.com.carrental.service;

import java.time.LocalDate;
import java.util.List;

import main.java.com.carrental.dao.VehicleDAO;
import main.java.com.carrental.model.Vehicle;
import main.java.com.carrental.model.Vehicle.VehicleStatus;
import main.java.com.carrental.model.Vehicle.VehicleType;
import main.java.com.carrental.util.JSONUtil;
import main.java.com.carrental.util.VehicleUtil;

public class VehicleService {
	
	
	/**
	 * gets all the vehicles with given json filters string
	 * @param json string, more then likely sent by the client
	 * @return list of vehicles based on the filters
	 */
	public List<Vehicle> getVehiclesFromFilters(String vehicleFilters) {
		
		VehicleDAO vecDAO = new VehicleDAO();
	    String carTypeStr = JSONUtil.extractField(vehicleFilters, "carType");
	    String startDateStr = JSONUtil.extractField(vehicleFilters, "startDate");
	    String endDateStr = JSONUtil.extractField(vehicleFilters, "endDate");
	    String priceSort = JSONUtil.extractField(vehicleFilters, "pricelimit");
	    String yearStr = JSONUtil.extractField(vehicleFilters, "year");
	    //String colour = JSONUtil.extractField(vehicleFilters, "colour");

	    VehicleType type = (carTypeStr != null && !carTypeStr.isEmpty()) 
	                       ? VehicleUtil.getTypeFromString(carTypeStr.toUpperCase()) : null;
	    LocalDate startDate = (startDateStr != null && !startDateStr.isEmpty()) 
	                          ? LocalDate.parse(startDateStr) : null;
	    LocalDate endDate = (endDateStr != null && !endDateStr.isEmpty()) 
	                        ? LocalDate.parse(endDateStr) : null;
	    int year = (yearStr != null && !yearStr.isEmpty()) 
	               ? Integer.parseInt(yearStr) : 0;
	    int pricelimit = (priceSort != null && !priceSort.isEmpty()) ? Integer.parseInt(priceSort) : Integer.MAX_VALUE;
	    
	    List<Vehicle> vehicles = vecDAO.searchVehicle(type, VehicleStatus.AVAILABLE, 
                startDate, endDate, pricelimit, year);
	    
	    return vehicles;
	}
	
}
