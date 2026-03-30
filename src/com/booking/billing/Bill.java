package com.booking.billing;

import java.util.List;

public class Bill {
    private final String billId;
    private final String showId;
    private final List<String> seatIds;
    private final double totalAmount;
    private final String strategyUsed;

    public Bill(String billId, String showId, List<String> seatIds, double totalAmount, String strategyUsed) {
        this.billId = billId;
        this.showId = showId;
        this.seatIds = seatIds;
        this.totalAmount = totalAmount;
        this.strategyUsed = strategyUsed;
    }

    public String getBillId() { return billId; }
    public String getShowId() { return showId; }
    public List<String> getSeatIds() { return seatIds; }
    public double getTotalAmount() { return totalAmount; }
    public String getStrategyUsed() { return strategyUsed; }

    @Override
    public String toString() {
        return "Bill{id=" + billId + ", show=" + showId + ", seats=" + seatIds +
               ", total=Rs." + totalAmount + ", strategy=" + strategyUsed + "}";
    }
}
