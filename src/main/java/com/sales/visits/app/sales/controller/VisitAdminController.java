package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.RejectRequest;
import com.sales.visits.app.sales.dto.request.VisitReviewRequest;
import com.sales.visits.app.sales.dto.response.VisitReviewDetailResponse;
import com.sales.visits.app.sales.mapper.VisitReviewMapper;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.service.VisitAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class VisitAdminController {
    private final VisitAdminService  visitAdminService;
    private final VisitReviewMapper mapper;


    public VisitAdminController(VisitAdminService visitAdminService, VisitReviewMapper mapper) {
        this.visitAdminService = visitAdminService;
        this.mapper = mapper;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') and " +
            "hasAuthority('ACCOUNT_EDIT')")
    public Page<VisitReviewDetailResponse> pending(Pageable pageable) {
        return visitAdminService.findPending(pageable).map(mapper::toDetail);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and " +
            "hasAuthority('ACCOUNT_EDIT')")
    public VisitReviewDetailResponse detail(@PathVariable Long id) {
        return mapper.toDetail(visitAdminService.getById(id));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN') and " +
            "hasAuthority('UPDATE_REQUEST_APPROVE')")
    public VisitReviewDetailResponse reviewAndApprove(
            @PathVariable Long id,
            @Valid @RequestBody VisitReviewRequest request,
            @AuthenticationPrincipal User admin) {
        Visit visit = visitAdminService.reviewAndApprove(id, request, admin);
        return mapper.toDetail(visit);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') and " +
            "hasAuthority('ACCOUNT_EDIT')")
    public VisitReviewDetailResponse reject(
            @PathVariable Long id,
            @RequestBody RejectRequest reason,
            @AuthenticationPrincipal User admin) {
        Visit visit = visitAdminService.reject(id, reason, admin);
        return mapper.toDetail(visit);
    }
}
