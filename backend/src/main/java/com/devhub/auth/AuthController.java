package com.devhub.auth;

import com.devhub.auth.dto.AuthResponse;
import com.devhub.auth.dto.LoginRequest;
import com.devhub.auth.dto.RegisterRequest;
import com.devhub.common.ApiException;
import com.devhub.users.User;
import com.devhub.users.UserService;
import com.devhub.users.dto.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
        
        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserMapper.toDto(user))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = tokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserMapper.toDto(user))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || !tokenProvider.validateToken(refreshToken)) {
            throw new ApiException("Invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        String newAccessToken = tokenProvider.generateAccessToken(email);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
