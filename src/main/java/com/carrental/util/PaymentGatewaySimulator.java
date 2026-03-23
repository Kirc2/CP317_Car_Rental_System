package main.java.com.carrental.util;

import java.util.UUID;

public class PaymentGatewaySimulator {
    /**
     * Simulates a payment transaction.
     * @param cardNumber the test card number (e.g., "4242424242424242" for success, "4000000000000002" for failure)
     * @param amount amount in dollars
     * @return a result map containing status ("SUCCESS" or "FAILED") and transaction ID (if success)
     */
    public static PaymentResult simulatePayment(String cardNumber, double amount) {
        // Simulate Stripe test cards: success for "4242...", failure for "4000..."
        if (cardNumber.startsWith("4242")) {
            return new PaymentResult("SUCCESS", UUID.randomUUID().toString());
        } else {
            return new PaymentResult("FAILED", null);
        }
    }

    public static class PaymentResult {
        private final String status;
        private final String transactionId;

        public PaymentResult(String status, String transactionId) {
            this.status = status;
            this.transactionId = transactionId;
        }

        public String getStatus() { return status; }
        public String getTransactionId() { return transactionId; }
    }
}
