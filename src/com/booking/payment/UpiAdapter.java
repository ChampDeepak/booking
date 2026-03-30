package com.booking.payment;

public class UpiAdapter implements IPaymentAdapter {
    @Override
    public boolean processPayment(String billId, double amount) {
        System.out.println("  [UpiAdapter] Processing UPI payment of Rs." + amount + " for bill " + billId);
        // simulate external UPI gateway call
        System.out.println("  [UpiAdapter] Payment successful via UPI");
        return true;
    }

    @Override
    public String getAdapterName() { return "UPI"; }
}
