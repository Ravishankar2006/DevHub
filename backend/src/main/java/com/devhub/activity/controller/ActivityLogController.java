package com.devhub.activity.controller;

import com.devhub.activity.ActivityLogService;
import com.devhub.activity.dto.ActivityLogDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLogDto>> list(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(activityLogService.listRecent(currentUser));
    }
}
