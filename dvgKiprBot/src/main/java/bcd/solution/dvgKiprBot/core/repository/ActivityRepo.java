package bcd.solution.dvgKiprBot.core.repository;

import bcd.solution.dvgKiprBot.core.models.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepo extends JpaRepository<Activity, Long>{
    List<Activity> findAllByIsDeleted(boolean isDeleted);
}
