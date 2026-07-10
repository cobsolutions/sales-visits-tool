package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.response.VisitReviewDetailResponse;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.service.ExcelReportService;
import com.sales.visits.app.sales.service.StatisticsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final ExcelReportService excelReportService;

    public StatisticsController(StatisticsService statisticsService, ExcelReportService excelReportService) {
        this.statisticsService = statisticsService;
        this.excelReportService = excelReportService;
    }

    @GetMapping("/my-visits")
    @PreAuthorize("hasRole('SALES_REP') and hasAuthority('VISIT_VIEW_OWN')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> findMyVisits(@AuthenticationPrincipal User currentUser, Pageable  pageable) {
        return ResponseEntity.ok(statisticsService.findMyActiveVisits(currentUser,pageable));
    }


    @GetMapping("/team-visits")
    @PreAuthorize("hasRole('TEAM_LEADER') and hasAuthority('VISIT_VIEW_TEAM')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> findMyTeamsVisits(@AuthenticationPrincipal User currentUser,
                                                                             Pageable  pageable) {
        return ResponseEntity.ok(statisticsService.findMyTeamActiveVisits(currentUser,pageable));
    }

    @GetMapping("/submitted-visits")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('VISIT_VIEW_ALL')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> findAllSubmittedVisits(@AuthenticationPrincipal User admin,
                                                                                  Pageable  pageable) {
        return ResponseEntity.ok(statisticsService.findAllSubmittedVisits(pageable));
    }

    @GetMapping("/filterBy-date/{visitDate}")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> filterByVisitDate(
            @AuthenticationPrincipal User currentUser,
            @PathVariable LocalDate visitDate,
            Pageable pageable) {
        if (currentUser.getRole() == UserRole.SALES_REP) {
            return ResponseEntity.ok(statisticsService.findByVisitDateAndVisitorId(visitDate, currentUser.getId(), pageable));
        }
        return ResponseEntity.ok(statisticsService.findByVisitDate(visitDate, pageable));
    }

    @GetMapping("/filterBy-org/{organizationName}")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> filterByOrganizationName(@AuthenticationPrincipal User currentUser,
                                                                                    @PathVariable String organizationName,
                                                                                    Pageable  pageable) {
        if (currentUser.getRole() == UserRole.SALES_REP) {
            return ResponseEntity.ok(statisticsService.findByOrganizationNameAndVisitorId(organizationName, currentUser.getId(), pageable));
        }
        return ResponseEntity.ok(statisticsService.findByOrganizationName(organizationName, pageable));
    }

    @GetMapping("/filterBy-name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public ResponseEntity<Page<VisitReviewDetailResponse>> filterBySubmittedBy(@AuthenticationPrincipal User currentUser,
                                                                               @PathVariable String name, Pageable  pageable) {
        return ResponseEntity.ok(statisticsService.findByVisitorUsername(name,pageable));
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public ResponseEntity<byte[]> exportVisitsReport(@AuthenticationPrincipal User currentUser,
                                                     @RequestParam(required = false) String filterType,
                                                     @RequestParam(required = false) String filterValue) {

        List<VisitReviewDetailResponse> visits =
                statisticsService.findAllForExport(currentUser, filterType, filterValue);

        byte[] excelBytes = excelReportService.generateVisitsReport(visits);
        String filename = "visits-report-" + LocalDate.now() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

}
