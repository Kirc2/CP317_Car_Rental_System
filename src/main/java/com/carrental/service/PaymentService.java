package main.java.com.carrental.service;

import java.time.LocalDate;

import main.java.com.carrental.dao.PaymentDAO;
import main.java.com.carrental.dao.RentalDAO;
import main.java.com.carrental.model.Payment;
import main.java.com.carrental.model.Rental;
import main.java.com.carrental.util.EmailUtil;
import main.java.com.carrental.util.PaymentGatewaySimulator;

public class PaymentService {
    private PaymentDAO paymentDAO = new PaymentDAO();
    private RentalDAO rentalDAO = new RentalDAO();
    private RentalService rentalService = new RentalService();
    private EmailUtil emailService = new EmailUtil(); // your existing email service

    /**
     * Process payment for a rental.
     * @param rentalId the rental ID
     * @param cardNumber the test card number (for simulation)
     * @return a result object indicating success/failure and payment ID
     */
    public PaymentResult processPayment(int rentalId, String cardNumber) {
        Rental rental = rentalDAO.findByID(cardNumber);
        if (rental == null) {
            return new PaymentResult(false, null, "Rental not found");
        }

        Payment payment = new Payment();
        payment.setRentalId(rentalId);
        payment.setAmount(rental.getTotalCost());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDate.now());

        // Simulate gateway call
        PaymentGatewaySimulator.PaymentResult gatewayResult = PaymentGatewaySimulator.simulatePayment(cardNumber, rental.getTotalCost());

        if ("SUCCESS".equals(gatewayResult.getStatus())) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(gatewayResult.getTransactionId());
            boolean saved = paymentDAO.createPayment(payment);
            if (saved) {
            	
                EmailUtil.sendEmail(rental.getCustomer().getEmail(), "Payment Confirmation", "This email has been sent just as a confirmation of your rental payment");
                return new PaymentResult(true, payment.getId(), "Payment successful");
            } else {
                return new PaymentResult(false, null, "Failed to record payment");
            }
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentDAO.createPayment(payment);
            return new PaymentResult(false, null, "Payment declined by gateway");
        }
    }

    public Payment getPaymentStatus(int paymentId) {
        return paymentDAO.findById(paymentId);
    }

    // Inner class for return result
    public static class PaymentResult {
        private final boolean success;
        private final Integer paymentId;
        private final String message;

        public PaymentResult(boolean success, Integer paymentId, String message) {
            this.success = success;
            this.paymentId = paymentId;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public Integer getPaymentId() { return paymentId; }
        public String getMessage() { return message; }
    }
}
