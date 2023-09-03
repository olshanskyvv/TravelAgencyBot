package bcd.solution.dvgKiprBot.core.services;

import bcd.solution.dvgKiprBot.core.models.User;
import bcd.solution.dvgKiprBot.core.models.UserRole;
import bcd.solution.dvgKiprBot.core.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    private UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User addUserIfNotExists(Long telegram_id, String username) {
        if (userRepository.existsById(telegram_id)) {
            return userRepository.getReferenceById(telegram_id);
        }
        User user = new User(telegram_id, username, null, null, UserRole.client);
        userRepository.save(user);
        return user;
    }

    public User setPhoneById(Long telegram_id, String phone) {
        User user = userRepository.findById(telegram_id).get();
        user.setPhone(phone);
        userRepository.save(user);
        return user;
    }

    public boolean hasPhoneById(Long telegram_id) {
        User user = userRepository.findById(telegram_id).get();
        return user.getPhone() != null;
    }

    public boolean isAuthorized(Long telegram_id) {
        UserRole userRole = userRepository.findById(telegram_id).get().getRole();
        return userRole == UserRole.partner || userRole == UserRole.admin;
    }
}
