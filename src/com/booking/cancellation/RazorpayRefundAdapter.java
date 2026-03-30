package com.booking.cancellation;

public class RazorpayRefundAdapter implements IRefundAdapter {
    @Override
    public boolean processRefund(String ticketId, double amount) {
        System.out.println("  [RazorpayRefundAdapter] Processing refund of Rs." + amount + " for ticket " + ticketId);
        System.out.println("  [RazorpayRefundAdapter] Refund successful via Razorpay");
        return true;
    }

    @Override
    public String getAdapterName() { return "Razorpay"; }
}
