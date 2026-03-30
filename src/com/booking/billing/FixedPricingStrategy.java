package com.booking.billing;

import com.booking.common.entity.Seat;
import com.booking.common.entity.Show;

public class FixedPricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Show show, Seat seat) {
        return show.getShowBasePrice() + seat.getBasePrice();
    }

    @Override
    public String getStrategyName() { return "FixedPricing"; }
}
