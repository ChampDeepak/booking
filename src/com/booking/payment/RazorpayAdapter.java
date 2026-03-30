package com.booking.payment;

public class RazorpayAdapter implements IPaymentAdapter {
    @Override
    public boolean processPayment(String billId, double amount) {
        System.out.println("  [RazorpayAdapter] Processing Razorpay payment of Rs." + amount + " for bill " + billId);
        // simulate external Razorpay gateway call
        System.out.println("  [RazorpayAdapter] Payment successful via Razorpay");
        return true;
    }

    @Override
    public String getAdapterName() { return "Razorpay"; }
}
