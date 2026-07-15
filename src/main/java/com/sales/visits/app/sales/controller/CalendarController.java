package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.CalendarVisitDto;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CalendarController {
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public ResponseEntity<List<CalendarVisitDto>> getCalendarVisits(@AuthenticationPrincipal User currentUser) {
        List<CalendarVisitDto> visits = calendarService.getUpcomingVisits(currentUser);
        return ResponseEntity.ok(visits);
    }

}
