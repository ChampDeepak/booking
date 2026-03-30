package com.booking.billing;

import com.booking.common.Database;
import com.booking.common.entity.Seat;
import com.booking.common.entity.Show;
import com.booking.common.exception.ValidationException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BillingService {
    private final Database db;
    private final ConcurrentHashMap<String, Bill> billStore = new ConcurrentHashMap<>();

    public BillingService(Database db) {
        this.db = db;
    }

    public Bill generateBill(String showId, List<String> seatIds, PricingStrategy strategy) {
        Show show = db.getShowById(showId);
        if (show == null) throw new ValidationException("Show not found: " + showId);

        double total = 0;
        for (String seatId : seatIds) {
            Seat seat = db.getSeatById(seatId);
            if (seat == null) throw new ValidationException("Seat not found: " + seatId);
            total += strategy.calculatePrice(show, seat);
        }

        String billId = "BILL-" + UUID.randomUUID().toString().substring(0, 8);
        Bill bill = new Bill(billId, showId, seatIds, total, strategy.getStrategyName());
        billStore.put(billId, bill);
        return bill;
    }

    public Bill getBillById(String billId) {
        return billStore.get(billId);
    }
}
