package com.devhub.projects.controller;

import com.devhub.projects.MilestoneService;
import com.devhub.projects.dto.MilestoneDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/milestones")
@RequiredArgsConstructor
public class MilestoneOverviewController {

    private final MilestoneService milestoneService;

    @GetMapping
    public ResponseEntity<List<MilestoneDto>> listAll(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(milestoneService.listAllMilestones(currentUser));
    }
}
