package com.booking.billing;

import com.booking.common.entity.Seat;
import com.booking.common.entity.Show;

public class PeakHourPricingStrategy implements PricingStrategy {
    private static final double PEAK_MULTIPLIER = 1.5;

    @Override
    public double calculatePrice(Show show, Seat seat) {
        return (show.getShowBasePrice() + seat.getBasePrice()) * PEAK_MULTIPLIER;
    }

    @Override
    public String getStrategyName() { return "PeakHourPricing (1.5x)"; }
}
