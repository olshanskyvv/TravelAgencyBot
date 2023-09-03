package bcd.solution.dvgKiprBot.core.repository;

import bcd.solution.dvgKiprBot.core.models.StateMachine;
import bcd.solution.dvgKiprBot.core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StateMachineRepo extends JpaRepository<StateMachine, Long> {
    Optional<StateMachine> findByUser(User user);

    boolean existsByUser(User user);


}
