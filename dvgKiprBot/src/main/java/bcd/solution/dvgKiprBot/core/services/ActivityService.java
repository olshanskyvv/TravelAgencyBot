package bcd.solution.dvgKiprBot.core.services;

import bcd.solution.dvgKiprBot.core.models.Activity;
import bcd.solution.dvgKiprBot.core.repository.ActivityRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepo activityRepo;

    @Autowired
    public ActivityService(ActivityRepo activityRepo) {
        this.activityRepo = activityRepo;
    }

    @Async
    public Activity getByIndex(Integer index) {
        return activityRepo.findAllByIsDeleted(false).get(index);
    }

    @Async
    public Activity getById(Long activityId) {
        return activityRepo.getReferenceById(activityId);
    }

    @Async
    public List<Activity> findAll() {
        return activityRepo.findAll();
    }
}
