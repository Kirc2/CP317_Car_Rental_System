package main.java.com.carrental.model;

import java.time.LocalDate;

public class Payment {
	
    private int id;
    private int rentalId;
    private double amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDate paymentDate;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    // Constructors, getters, setters
    public Payment() {}

    public Payment(int rentalId, double amount, PaymentStatus status, String transactionId, LocalDate paymentDate) {
        this.rentalId = rentalId;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
        this.paymentDate = paymentDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRentalId() { return rentalId; }
    public void setRentalId(int rentalId) { this.rentalId = rentalId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}
