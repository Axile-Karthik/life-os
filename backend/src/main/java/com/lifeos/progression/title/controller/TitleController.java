package com.lifeos.progression.title.controller;

import com.lifeos.progression.title.dto.TitleResponse;
import com.lifeos.progression.title.entity.Title;
import com.lifeos.progression.title.mapper.TitleMapper;
import com.lifeos.progression.title.service.TitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/titles")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;
    private final TitleMapper titleMapper;

    /**
     * GET /api/titles
     * Retrieves all available titles in the progression system.
     */
    @GetMapping
    public ResponseEntity<List<TitleResponse>> getAllTitles() {
        List<Title> titles = titleService.getAllTitles();
        List<TitleResponse> responses = titles.stream()
                .map(titleMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
