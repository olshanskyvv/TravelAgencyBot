package bcd.solution.dvgKiprBot.core.repository;;

import bcd.solution.dvgKiprBot.core.models.HotelFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelFeatureRepo extends JpaRepository<HotelFeature, Long> {
    HotelFeature findFirstByNameAndDescription(String name, String description);
}
