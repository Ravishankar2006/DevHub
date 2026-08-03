package com.devhub.users.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private String theme;
    private boolean githubConnected;
    private boolean dailyBriefEnabled;
}
