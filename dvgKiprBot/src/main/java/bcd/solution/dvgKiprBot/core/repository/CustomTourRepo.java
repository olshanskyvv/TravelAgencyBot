package bcd.solution.dvgKiprBot.core.repository;

import bcd.solution.dvgKiprBot.core.models.CustomTour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomTourRepo extends JpaRepository<CustomTour, Long> {
    List<CustomTour> findAllByIsDeleted(boolean isDeleted);

    long countByIsDeleted(boolean isDeleted);
}
