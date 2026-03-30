package com.booking.fetchshow;

import com.booking.common.entity.Show;
import java.util.List;

public interface IFetchShowService {
    List<Show> getShowsByMovie(String movieId);
}
