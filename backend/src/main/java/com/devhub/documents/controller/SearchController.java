package com.devhub.documents.controller;

import com.devhub.documents.SearchService;
import com.devhub.documents.dto.SearchResultDto;
import com.devhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchResultDto>> search(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("q") String query) {
        return ResponseEntity.ok(searchService.search(currentUser, query));
    }
}
