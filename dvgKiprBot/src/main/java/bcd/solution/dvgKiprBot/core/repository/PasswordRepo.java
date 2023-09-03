package bcd.solution.dvgKiprBot.core.repository;

import bcd.solution.dvgKiprBot.core.models.Password;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepo extends JpaRepository<Password, String>{
    boolean existsByAvailablePassword(String password);
    void deleteById(String password);
}
