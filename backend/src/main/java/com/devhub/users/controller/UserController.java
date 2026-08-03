package com.devhub.users.controller;

import com.devhub.users.User;
import com.devhub.users.UserService;
import com.devhub.users.dto.UserDto;
import com.devhub.users.dto.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal User currentUser) {
        // Since we loaded user in filter, we can map it to DTO directly
        return ResponseEntity.ok(UserMapper.toDto(currentUser));
    }

    @PutMapping("/me/theme")
    public ResponseEntity<UserDto> updateTheme(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> body) {
        String theme = body.getOrDefault("theme", "light");
        userService.updateTheme(currentUser.getId(), theme);
        
        // Refresh settings in user object for DTO response
        User updatedUser = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(UserMapper.toDto(updatedUser));
    }
}
