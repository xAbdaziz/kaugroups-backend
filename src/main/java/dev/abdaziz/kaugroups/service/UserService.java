package dev.abdaziz.kaugroups.service;

import dev.abdaziz.kaugroups.exception.BusinessRuleViolationException;
import dev.abdaziz.kaugroups.exception.ResourceNotFoundException;
import dev.abdaziz.kaugroups.model.Gender;
import dev.abdaziz.kaugroups.model.User;
import dev.abdaziz.kaugroups.repository.UserRepository;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new IllegalArgumentException("Email not found from OAuth2 provider");
        }

        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name != null ? name : email.split("@")[0])
                            .build();
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public void updateGender(UUID id, Gender gender) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (user.getGender() != Gender.UNKNOWN) {
            throw new BusinessRuleViolationException("User already has a gender assigned");
        }
        
        user.setGender(gender);
        userRepository.save(user);
    }

}

