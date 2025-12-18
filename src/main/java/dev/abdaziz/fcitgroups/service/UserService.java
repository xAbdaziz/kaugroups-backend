package dev.abdaziz.fcitgroups.service;

import dev.abdaziz.fcitgroups.model.Gender;
import dev.abdaziz.fcitgroups.model.User;
import dev.abdaziz.fcitgroups.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        User user = User.builder()
                .name(name)
                .email(email)
                .gender(Gender.UNKNOWN)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public void updateGender(UUID id, Gender gender) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        if (user.get().getGender() != Gender.UNKNOWN) {
            throw new IllegalArgumentException("User already has a gender");
        }
        user.get().setGender(gender);
        userRepository.save(user.get());
    }
}