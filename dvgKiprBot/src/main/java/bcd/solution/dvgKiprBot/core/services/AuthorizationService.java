package bcd.solution.dvgKiprBot.core.services;

import bcd.solution.dvgKiprBot.core.models.User;
import bcd.solution.dvgKiprBot.core.models.UserRole;
import bcd.solution.dvgKiprBot.core.repository.PasswordRepo;
import bcd.solution.dvgKiprBot.core.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    private final UserRepository userRepository;
    private final PasswordRepo passwordRepo;

    public AuthorizationService(UserRepository userRepository,
                                PasswordRepo passwordRepo) {
        this.userRepository = userRepository;
        this.passwordRepo = passwordRepo;
    }

    @Async
    @SneakyThrows
    public boolean authByPassword(Long userId, String password) {
        if (passwordRepo.existsByAvailablePassword(password)) {

            User user = userRepository.findById(userId).get();
            user.setRole(UserRole.partner);
            userRepository.save(user);

            passwordRepo.deleteById(password);

            return true;
        }
        return false;
    }

    @Async
    @SneakyThrows
    public boolean isAuthorized(Long userId) {
        User user = userRepository.findById(userId).get();
        return user.getRole() == UserRole.partner || user.getRole() == UserRole.admin;
    }
}
