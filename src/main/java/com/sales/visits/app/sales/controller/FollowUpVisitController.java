package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.FollowUpVisitRequest;
import com.sales.visits.app.sales.dto.response.AccountReviewDetail;
import com.sales.visits.app.sales.dto.response.PhysicianReviewDetail;
import com.sales.visits.app.sales.dto.response.PhysicianSummaryResponse;
import com.sales.visits.app.sales.dto.response.VisitResponse;
import com.sales.visits.app.sales.mapper.VisitMapper;
import com.sales.visits.app.sales.mapper.VisitReviewMapper;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.service.FollowUpVisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FollowUpVisitController {
    private final FollowUpVisitService followUpVisitService;
    private final VisitReviewMapper visitReviewMapper;
    private final VisitMapper visitMapper;


    public FollowUpVisitController(FollowUpVisitService followUpVisitService, VisitReviewMapper visitReviewMapper, VisitMapper visitMapper) {
        this.followUpVisitService = followUpVisitService;
        this.visitReviewMapper = visitReviewMapper;
        this.visitMapper = visitMapper;
    }

    @GetMapping("/accounts/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_FOLLOWUP')")
    public AccountReviewDetail getAccountDetails(@PathVariable Long id){
        return visitReviewMapper.toAccountDetail(followUpVisitService.getAccountDetails(id));
    }

    @GetMapping("/accounts/active")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_FOLLOWUP')")
    public List<AccountReviewDetail> getActiveAccounts(){
        return visitReviewMapper.toAccountsDetail(followUpVisitService.getActiveAccounts());
    }


    @GetMapping("/physicians/active")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_FOLLOWUP')")
    public List<PhysicianReviewDetail> getActivePhysicians(){
        return visitReviewMapper.toPhysiciansDetail(followUpVisitService.getActivePhysicians());
    }

    @GetMapping("/active-physicians/account/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_FOLLOWUP')")
    public List<PhysicianSummaryResponse> getActivePhysiciansByAccountId(@PathVariable Long id){
        return followUpVisitService.findActivePhysiciansForAccount(id);
    }

    @PostMapping("/follow-up-visit")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_FOLLOWUP')")
    public ResponseEntity<VisitResponse> followUpVisit(@Valid @RequestBody FollowUpVisitRequest request,
                                                       @AuthenticationPrincipal User currentUser){
        Visit visit = followUpVisitService.followUpVisit(request,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(visitMapper.toResponse(visit));
    }
}
