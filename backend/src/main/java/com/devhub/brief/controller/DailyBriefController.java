package com.devhub.brief.controller;

import com.devhub.brief.DailyBriefService;
import com.devhub.brief.dto.DailyBriefDto;
import com.devhub.jobs.dto.AiJobDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/brief")
@RequiredArgsConstructor
public class DailyBriefController {

    private final DailyBriefService dailyBriefService;

    @PostMapping("/generate")
    public ResponseEntity<AiJobDto> generate(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(dailyBriefService.triggerGeneration(currentUser));
    }

    @GetMapping("/today")
    public ResponseEntity<DailyBriefDto> today(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(dailyBriefService.getTodayBrief(currentUser));
    }
}
