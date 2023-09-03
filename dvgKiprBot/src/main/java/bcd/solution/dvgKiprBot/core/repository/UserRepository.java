package bcd.solution.dvgKiprBot.core.repository;

import bcd.solution.dvgKiprBot.core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
