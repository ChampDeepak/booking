package com.booking.admin;

import com.booking.common.entity.Show;

public interface IAdminService {
    Show addShow(String showId, String movieId, String theaterId, String auditoriumId,
                 String startTime, String endTime, double showBasePrice);
}
