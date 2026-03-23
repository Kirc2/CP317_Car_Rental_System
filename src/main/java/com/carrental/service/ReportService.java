package main.java.com.carrental.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import main.java.com.carrental.dao.ReportDAO;
import main.java.com.carrental.util.JSONUtil;

public class ReportService {
    private ReportDAO reportDAO = new ReportDAO();

    /**
     * Gets the daily report based on the date
     * @param String for the date
     * @return report returned based on the date
     */
    public String getDailyReportJson(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            Map<String, Object> report = reportDAO.getDailyReport(date);
            return JSONUtil.mapToJson(report);
        } catch (DateTimeParseException e) {
            return "{\"error\":\"Invalid date format. Use YYYY-MM-DD\"}";
        }
    }
    
    /**
     * self explanitory
     * @return popular cars in json format to send over
     */
    public String getPopularCarsJson() {
        List<Map<String, Object>> popular = reportDAO.getPopularCars();
        return JSONUtil.listMapToJson(popular);
    }

    
    
    public String getMonthlyRevenueJson(int year, int month) {
        double revenue = reportDAO.getMonthlyRevenue(year, month);
        return "{\"year\":" + year + ",\"month\":" + month + ",\"totalRevenue\":" + revenue + "}";
    } 
}
