package com.sales.visits.app.sales.controller;

import com.sales.visits.app.sales.dto.request.CreateParentOrganizationRequest;
import com.sales.visits.app.sales.dto.request.CreatePtocLocation;
import com.sales.visits.app.sales.dto.request.NewVisitRequest;
import com.sales.visits.app.sales.dto.response.ParentOrganizationResponse;
import com.sales.visits.app.sales.dto.response.PtocLocationResponse;
import com.sales.visits.app.sales.dto.response.UserSummaryResponse;
import com.sales.visits.app.sales.dto.response.VisitResponse;
import com.sales.visits.app.sales.mapper.VisitMapper;
import com.sales.visits.app.sales.model.entity.ParentOrganization;
import com.sales.visits.app.sales.model.entity.PtocLocation;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.repository.ParentOrganizationRepository;
import com.sales.visits.app.sales.repository.PtocLocationRepository;
import com.sales.visits.app.sales.service.NewVisitService;
import com.sales.visits.app.sales.service.PhysicianResolutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NewVisitController {
    private final NewVisitService newVisitService;
    private final VisitMapper visitMapper;
    private final PhysicianResolutionService physicianResolutionService;
    private final ParentOrganizationRepository parentOrganizationRepository;
    private final PtocLocationRepository ptocLocationRepository;

    public NewVisitController(NewVisitService newVisitService, VisitMapper visitMapper, PhysicianResolutionService physicianResolutionService, ParentOrganizationRepository parentOrganizationRepository, PtocLocationRepository ptocLocationRepository) {
        this.newVisitService = newVisitService;
        this.visitMapper = visitMapper;
        this.physicianResolutionService = physicianResolutionService;
        this.parentOrganizationRepository = parentOrganizationRepository;
        this.ptocLocationRepository = ptocLocationRepository;
    }

    @PostMapping("/new-visit")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_NEW')")
    public ResponseEntity<VisitResponse> newVisit(@Valid @RequestBody NewVisitRequest request,
                                                  @AuthenticationPrincipal User currentUser) {
        Visit visit = newVisitService.createNewVisit(request,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(visitMapper.toResponse(visit));
    }

    @GetMapping("/joined-visitor")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public List<UserSummaryResponse> joinedVisitors(@AuthenticationPrincipal User currentUser){
        return newVisitService.findJoinedVisitors(currentUser.getId()).stream()
                .map(u->new UserSummaryResponse(u.getId(),u.getUsername(),u.getRole())).toList();
    }

    @GetMapping("/exists/{npi}")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public ResponseEntity<Map<String, Boolean>> checkNpiExists(@PathVariable String npi) {
        return ResponseEntity.ok(Map.of("exists", physicianResolutionService.existsByNpi(npi)));
    }

    @GetMapping("/parent-organizations")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public List<ParentOrganizationResponse> listParentOrganization() {
        return parentOrganizationRepository.findAllByOrderByNameAsc().stream()
                .map(o -> new ParentOrganizationResponse(o.getId(), o.getName()))
                .toList();
    }

    @PostMapping("/parent-organizations")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_NEW')")
    public ResponseEntity<ParentOrganizationResponse> createNewParentOrganization(@Valid @RequestBody CreateParentOrganizationRequest req) {
        ParentOrganization org = parentOrganizationRepository.findByName(req.name().trim())
                .orElseGet(() -> parentOrganizationRepository.save(
                        ParentOrganization.builder().name(req.name().trim()).build()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ParentOrganizationResponse(org.getId(), org.getName()));
    }

    @GetMapping("/ptoc-locations")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP')")
    public List<PtocLocationResponse> listPtocLocation() {
        return ptocLocationRepository.findAllByOrderByNameAsc().stream()
                .map(o -> new PtocLocationResponse(o.getId(), o.getName()))
                .toList();
    }

    @PostMapping("/ptoc-locations")
    @PreAuthorize("hasAnyRole('ADMIN','TEAM_LEADER','SALES_REP') and hasAuthority('VISIT_CREATE_NEW')")
    public ResponseEntity<PtocLocationResponse> createNewPtocLocation(@Valid @RequestBody CreatePtocLocation req) {
        PtocLocation ptocLocation = ptocLocationRepository.findByName(req.name().trim())
                .orElseGet(() -> ptocLocationRepository.save(
                        PtocLocation.builder().name(req.name().trim()).build()
                ));
        return ResponseEntity.status(HttpStatus.CREATED).body(new PtocLocationResponse(ptocLocation.getId(), ptocLocation.getName()));
    }
}
