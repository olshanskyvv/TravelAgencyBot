package bcd.solution.dvgKiprBot.core.services;

import bcd.solution.dvgKiprBot.core.models.Activity;
import bcd.solution.dvgKiprBot.core.models.Hotel;
import bcd.solution.dvgKiprBot.core.models.Resort;
import bcd.solution.dvgKiprBot.core.models.Stars;
import bcd.solution.dvgKiprBot.core.repository.HotelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    private final HotelRepo hotelRepo;

    @Autowired
    public HotelService(HotelRepo hotelRepo) {
        this.hotelRepo = hotelRepo;
    }

    public Hotel getByIndex(Integer index) {
        return hotelRepo.findAllByIsDeleted(false).get(index);
    }

    @Async
    public List<Hotel> findByResort(Resort resort) {
        if (resort == null) {
            return hotelRepo.findAllByIsDeleted(false);
        }
        return hotelRepo.findAllByResortAndIsDeleted(resort, false);
    }

    @Async
    public List<Hotel> findByResortAndActivitiesAndStars(Resort resort, List<Activity> activities, Stars stars) {
        if (activities.isEmpty()) {
            return findByResortAndStars(resort, stars);
        }
        if (stars == null) {
            return findByResort(resort).stream()
                            .filter((hotel) -> new HashSet<>(hotel.activities).containsAll(activities)).toList();
        }
        List<Hotel> hotels;
        if (resort == null) {
            hotels = hotelRepo.findAllByStarsAndIsDeleted(stars, false);
            if (activities.isEmpty()) {
                return hotels;
            } else {
                return hotels.stream()
                        .filter((hotel) -> new HashSet<>(hotel.activities).containsAll(activities)).toList();
            }
        }
        hotels = hotelRepo.findAllByResortAndStarsAndIsDeleted(resort, stars, false);
        if (activities.isEmpty()) {
            return hotels;
        }
        return hotels.stream()
                .filter((hotel) -> new HashSet<>(hotel.activities).containsAll(activities)).toList();
    }

    @Async
    public List<Hotel> findByResortAndActivities(Resort resort, List<Activity> activities) {
        List<Hotel> hotels;
        if (resort == null) {
            hotels = hotelRepo.findAllByIsDeleted(false);
            if (activities.isEmpty()) {
                return hotels;
            } else {
                return hotels.stream()
                        .filter((hotel) -> new HashSet<>(hotel.activities).containsAll(activities)).toList();
            }
        }
        if (activities.isEmpty()) {
            return hotelRepo.findAllByResortAndIsDeleted(resort, false);
        }
        return findByResort(resort).stream()
                .filter((hotel) -> new HashSet<>(hotel.activities).containsAll(activities)).toList();
    }

    public Optional<Hotel> getById(Long hotelId) {
        return hotelRepo.findById(hotelId);
    }

    public List<Hotel> findByResortAndStars(Resort resort, Stars stars) {
        if (resort == null) {
            if (stars == null) {
                return hotelRepo.findAllByIsDeleted(false);
            }
            return hotelRepo.findAllByStarsAndIsDeleted(stars, false);
        }
        if (stars == null) {
            return hotelRepo.findAllByResortAndIsDeleted(resort, false);
        }
        return hotelRepo.findAllByResortAndStarsAndIsDeleted(resort, stars, false);
    }

}
