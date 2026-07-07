package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.response.VisitReviewDetailResponse;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.service.StatisticsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/my-visits")
    @PreAuthorize("hasRole('SALES_REP') and hasAuthority('VISIT_VIEW_OWN')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> findMyVisits(@AuthenticationPrincipal User currentUser, Pageable  pageable) {
        return ResponseEntity.ok(statisticsService.findMyActiveVisits(currentUser,pageable));
    }


    @GetMapping("/team-visits")
    @PreAuthorize("hasRole('TEAM_LEADER') and hasAuthority('VISIT_VIEW_TEAM')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> findMyTeamsVisits(@AuthenticationPrincipal User currentUser, Pageable  pageable) {
        return ResponseEntity.ok(statisticsService.findMyTeamActiveVisits(currentUser,pageable));
    }
}
