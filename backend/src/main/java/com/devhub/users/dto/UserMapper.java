package com.devhub.users.dto;

import com.devhub.users.User;
import com.devhub.users.UserSettings;

public class UserMapper {
    public static UserDto toDto(User user) {
        if (user == null) return null;

        UserSettings settings = user.getSettings();
        String theme = settings != null ? settings.getTheme() : "light";
        boolean githubConnected = settings != null && settings.isGithubConnected();
        boolean dailyBriefEnabled = settings == null || settings.isDailyBriefEnabled();

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .theme(theme)
                .githubConnected(githubConnected)
                .dailyBriefEnabled(dailyBriefEnabled)
                .build();
    }
}
