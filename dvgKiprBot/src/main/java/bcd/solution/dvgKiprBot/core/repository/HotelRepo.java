package bcd.solution.dvgKiprBot.core.repository;

import bcd.solution.dvgKiprBot.core.models.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepo extends JpaRepository<Hotel, Long> {
    List<Hotel> findAllByResortAndIsDeleted(Resort resort, boolean isDeleted);

    List<Hotel> findAllByResortAndStarsAndIsDeleted(Resort resort, Stars stars, boolean isDeleted);
    List<Hotel> findAllByStarsAndIsDeleted(Stars stars, boolean isDeleted);

    List<Hotel> findAllByIsDeleted(boolean isDeleted);
}
