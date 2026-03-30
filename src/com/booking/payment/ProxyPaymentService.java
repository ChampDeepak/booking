package com.booking.payment;

import com.booking.billing.Bill;
import com.booking.billing.BillingService;
import com.booking.common.Database;
import com.booking.common.entity.Ticket;
import com.booking.common.exception.ValidationException;

public class ProxyPaymentService {
    private final PaymentService paymentService;
    private final BillingService billingService;
    private final Database db;

    public ProxyPaymentService(PaymentService paymentService, BillingService billingService, Database db) {
        this.paymentService = paymentService;
        this.billingService = billingService;
        this.db = db;
    }

    public Ticket makePayment(String billId, String userEmail, IPaymentAdapter adapter) {
        if (billId == null || billId.isBlank()) {
            throw new ValidationException("Bill ID cannot be empty");
        }
        if (userEmail == null || userEmail.isBlank()) {
            throw new ValidationException("User email cannot be empty");
        }
        Bill bill = billingService.getBillById(billId);
        if (bill == null) {
            throw new ValidationException("Bill not found: " + billId);
        }
        if (db.getUserByEmail(userEmail) == null) {
            throw new ValidationException("User not found: " + userEmail);
        }

        paymentService.setPaymentAdapter(adapter);
        return paymentService.makePayment(billId, userEmail);
    }
}
