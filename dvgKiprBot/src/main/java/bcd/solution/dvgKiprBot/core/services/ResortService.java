package bcd.solution.dvgKiprBot.core.services;

import bcd.solution.dvgKiprBot.core.models.Activity;
import bcd.solution.dvgKiprBot.core.models.Hotel;
import bcd.solution.dvgKiprBot.core.models.Resort;
import bcd.solution.dvgKiprBot.core.repository.HotelRepo;
import bcd.solution.dvgKiprBot.core.repository.ResortRepo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class ResortService {
    private final ResortRepo resortRepo;
    private final HotelRepo hotelRepo;

    public ResortService(ResortRepo resortRepo,
                         HotelRepo hotelRepo) {
        this.hotelRepo = hotelRepo;
        this.resortRepo = resortRepo;
    }

//    @Async
//    public List<Resort> getByActivities(List<Activity> activities) {
//        if (activities.isEmpty()) {
//            return resortRepo.findAllByIsDeleted(false);
//        }
//        List<Resort> allResorts = resortRepo.findAllByIsDeleted(false);
//        return allResorts.stream()
//                .filter((resort) -> new HashSet<>(resort.activities).containsAll(activities)).toList();
//    }

    @Async
    public List<Resort> getByActivities(List<Activity> activities) {
        if (activities.isEmpty()) {
            return resortRepo.findAllByIsDeleted(false);
        }
        List<Hotel> hotels = hotelRepo.findAllByIsDeleted(false);
        hotels = hotels.stream()
                .filter((hotel) -> new HashSet<>(hotel.activities).containsAll(activities)).toList();
        return hotels.stream().map(Hotel::getResort).distinct().toList();
    }

    @Async
    public Optional<Resort> getById(Long resortId) {
//        Resort resort = resortRepo.getReferenceById(resortId);
//        return resort;
        Optional<Resort> resort = resortRepo.findById(resortId);
        return resort;
    }
}
