package main.java.com.carrental.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import main.java.com.carrental.model.Payment;

public class PaymentDAO {
	  
	
	public boolean createPayment(Payment payment) {
	        String sql = "INSERT INTO payments (rental_id, amount, status, transaction_id, payment_date) VALUES (?, ?, ?, ?, ?)";
	        boolean rows = MySQL.update(sql,
	            payment.getRentalId(),
	            payment.getAmount(),
	            payment.getStatus().name(),
	            payment.getTransactionId(),
	            payment.getPaymentDate()
	        );
	        return rows;
	    }

	    public Payment findById(int id) {
	        String sql = "SELECT * FROM payments WHERE id = ?";
	        ResultSet rs = MySQL.fetch(sql, id);
	        try {
	            if (rs != null && rs.next()) {
	                return mapRowToPayment(rs);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    public Payment findByRentalId(int rentalId) {
	        String sql = "SELECT * FROM payments WHERE rental_id = ?";
	        ResultSet rs = MySQL.fetch(sql, rentalId);
	        try {
	            if (rs != null && rs.next()) {
	                return mapRowToPayment(rs);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	    public boolean updateStatus(int paymentId, Payment.PaymentStatus status, String transactionId) {
	        String sql = "UPDATE payments SET status = ?, transaction_id = ? WHERE id = ?";
	        boolean rows = MySQL.update(sql, status.name(), transactionId, paymentId);
	        return rows;
	    }

	    
	    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
	        Payment payment = new Payment();
	        payment.setId(rs.getInt("id"));
	        payment.setRentalId(rs.getInt("rental_id"));
	        payment.setAmount(rs.getDouble("amount"));
	        payment.setStatus(Payment.PaymentStatus.valueOf(rs.getString("status")));
	        payment.setTransactionId(rs.getString("transaction_id"));
	        java.sql.Date ts = rs.getDate("payment_date");
	        if (ts != null) payment.setPaymentDate(ts.toLocalDate());
	        return payment;
	    }
}
