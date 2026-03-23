package main.java.com.carrental.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {
	
	
	
    public Map<String, Object> getDailyReport(LocalDate date) {
        Map<String, Object> report = new HashMap<>();
        String sql = "SELECT COUNT(*) AS rental_count, SUM(total_cost) AS total_revenue " +
                     "FROM rentals WHERE DATE(start_date) = ?";
        ResultSet rs = MySQL.fetch(sql, date);
        try {
            if (rs != null && rs.next()) {
                report.put("date", date.toString());
                report.put("rentalCount", rs.getInt("rental_count"));
                report.put("totalRevenue", rs.getDouble("total_revenue"));
            } else {
                report.put("date", date.toString());
                report.put("rentalCount", 0);
                report.put("totalRevenue", 0.0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    // Popular car types: list of (type, rental count) sorted descending
    public List<Map<String, Object>> getPopularCars() {
        List<Map<String, Object>> popular = new ArrayList<>();
        String sql = "SELECT v.vehicle_type, COUNT(r.id) AS rental_count " +
                     "FROM vehicles v LEFT JOIN rentals r ON v.id = r.vehicle_id " +
                     "GROUP BY v.vehicle_type ORDER BY rental_count DESC";
        ResultSet rs = MySQL.fetch(sql);
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("type", rs.getString("vehicle_type"));
                entry.put("count", rs.getInt("rental_count"));
                popular.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return popular;
    }

    // Monthly revenue: sum of total_cost for a given year and month
    public double getMonthlyRevenue(int year, int month) {
        String sql = "SELECT SUM(total_cost) AS total FROM rentals " +
                     "WHERE YEAR(start_date) = ? AND MONTH(start_date) = ?";
        ResultSet rs = MySQL.fetch(sql, year, month);
        try {
            if (rs != null && rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
	
}
