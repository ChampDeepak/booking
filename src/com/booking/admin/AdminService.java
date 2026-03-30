package com.booking.admin;

import com.booking.common.Database;
import com.booking.common.entity.Show;

public class AdminService implements IAdminService {
    private final Database db;

    public AdminService(Database db) {
        this.db = db;
    }

    @Override
    public Show addShow(String showId, String movieId, String theaterId, String auditoriumId,
                        String startTime, String endTime, double showBasePrice) {
        Show show = new Show(showId, movieId, theaterId, auditoriumId, startTime, endTime, showBasePrice);
        db.addShow(show);
        System.out.println("  [AdminService] Show added: " + show);
        return show;
    }
}
