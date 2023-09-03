package bcd.solution.dvgKiprBot.core.services;

import org.springframework.beans.factory.annotation.Autowired;
import bcd.solution.dvgKiprBot.core.models.CustomTour;
import bcd.solution.dvgKiprBot.core.repository.CustomTourRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomToursService {
    private final CustomTourRepo customTourRepo;
    @Autowired
    public CustomToursService(CustomTourRepo customTourRepo) {
        this.customTourRepo = customTourRepo;
    }

    public CustomTour getByIndex(Integer index) {
        return customTourRepo.findAllByIsDeleted(false).get(index);
    }

    public Optional<CustomTour> getById(Long customTourId) {
        return customTourRepo.findById(customTourId);
    }
}
