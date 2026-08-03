package com.devhub.users;

import com.devhub.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String name, String email, String plainPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new ApiException("Email is already in use", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(plainPassword))
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        UserSettings settings = UserSettings.builder()
                .user(user)
                .theme("light")
                .githubConnected(false)
                .dailyBriefEnabled(true)
                .build();

        userSettingsRepository.save(settings);
        user.setSettings(settings);

        return user;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public UserSettings updateTheme(UUID userId, String theme) {
        if (!theme.equals("light") && !theme.equals("dark")) {
            throw new ApiException("Invalid theme. Must be 'light' or 'dark'", HttpStatus.BAD_REQUEST);
        }

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND));

        settings.setTheme(theme);
        return userSettingsRepository.save(settings);
    }
}
