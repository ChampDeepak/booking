package com.booking.billing;

import com.booking.common.entity.Seat;
import com.booking.common.entity.Show;

public interface PricingStrategy {
    double calculatePrice(Show show, Seat seat);
    String getStrategyName();
}
