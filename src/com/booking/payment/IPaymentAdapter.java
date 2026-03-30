package com.booking.payment;

public interface IPaymentAdapter {
    boolean processPayment(String billId, double amount);
    String getAdapterName();
}
