package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.NewVisitRequest;
import com.sales.visits.app.sales.dto.response.UserSummaryResponse;
import com.sales.visits.app.sales.dto.response.VisitResponse;
import com.sales.visits.app.sales.mapper.VisitMapper;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.service.NewVisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NewVisitController {
    private final NewVisitService newVisitService;
    private final VisitMapper visitMapper;

    public NewVisitController(NewVisitService newVisitService, VisitMapper visitMapper) {
        this.newVisitService = newVisitService;
        this.visitMapper = visitMapper;
    }

    @PostMapping("/new-visit")
    @PreAuthorize("hasAnyRole('TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_NEW')")
    public ResponseEntity<VisitResponse> newVisit(@Valid @RequestBody NewVisitRequest request,
                                                  @AuthenticationPrincipal User currentUser) {
        Visit visit = newVisitService.createNewVisit(request,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(visitMapper.toResponse(visit));
    }

    @GetMapping("/joined-visitor")
    @PreAuthorize("hasAnyRole('TEAM_LEADER','SALES_REP')")
    public List<UserSummaryResponse> joinedVisitors(){
        return newVisitService.findJoinedVisitors().stream()
                .map(u->new UserSummaryResponse(u.getUsername(),u.getRole())).toList();
    }
}
