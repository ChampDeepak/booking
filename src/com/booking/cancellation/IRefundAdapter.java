package com.booking.cancellation;

public interface IRefundAdapter {
    boolean processRefund(String ticketId, double amount);
    String getAdapterName();
}
