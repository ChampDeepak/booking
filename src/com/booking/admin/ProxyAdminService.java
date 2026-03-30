package com.booking.admin;

import com.booking.common.Database;
import com.booking.common.entity.Show;
import com.booking.common.entity.User;
import com.booking.common.enums.UserRole;
import com.booking.common.exception.ValidationException;

public class ProxyAdminService implements IAdminService {
    private final IAdminService service;
    private final Database db;

    public ProxyAdminService(IAdminService service, Database db) {
        this.service = service;
        this.db = db;
    }

    @Override
    public Show addShow(String showId, String movieId, String theaterId, String auditoriumId,
                        String startTime, String endTime, double showBasePrice) {
        return service.addShow(showId, movieId, theaterId, auditoriumId, startTime, endTime, showBasePrice);
    }

    // wrapper that checks role before delegating
    public Show addShowAsUser(String userEmail, String showId, String movieId, String theaterId,
                              String auditoriumId, String startTime, String endTime, double showBasePrice) {
        User user = db.getUserByEmail(userEmail);
        if (user == null) {
            throw new ValidationException("User not found: " + userEmail);
        }
        if (user.getRole() != UserRole.ADMIN) {
            throw new ValidationException("Admin role required. User " + userEmail + " has role: " + user.getRole());
        }
        return service.addShow(showId, movieId, theaterId, auditoriumId, startTime, endTime, showBasePrice);
    }
}
