package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.request.FollowUpVisitRequest;
import com.sales.visits.app.sales.dto.request.PhysicianFieldsRequest;
import com.sales.visits.app.sales.dto.response.PhysicianSummaryResponse;
import com.sales.visits.app.sales.exception.EntityNotFoundException;
import com.sales.visits.app.sales.model.entity.*;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.VisitType;
import com.sales.visits.app.sales.repository.AccountRepository;
import com.sales.visits.app.sales.repository.PhysicianRepository;
import com.sales.visits.app.sales.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FollowUpVisitService {
    private final AccountRepository accountRepository;
    private final PhysicianRepository physicianRepository;
    private final NewVisitService newVisitService;
    private final PhysicianResolutionService physicianResolutionService;
    private final VisitRepository visitRepository;

    public FollowUpVisitService(AccountRepository accountRepository, PhysicianRepository physicianRepository, NewVisitService newVisitService, PhysicianResolutionService physicianResolutionService, VisitRepository visitRepository) {
        this.accountRepository = accountRepository;
        this.physicianRepository = physicianRepository;
        this.newVisitService = newVisitService;
        this.physicianResolutionService = physicianResolutionService;
        this.visitRepository = visitRepository;
    }

    @Transactional
    public Visit followUpVisit(FollowUpVisitRequest followUpVisitRequest, User currentUser){
        Account account = accountRepository.findById(followUpVisitRequest.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if(!account.getStatus().equals(ApprovalStatus.ACTIVE)){
            throw new IllegalStateException("Follow-up visits can only be created for an active account");
        }

        User joinedVisitor = newVisitService.resolveJoinedVisitor
                (followUpVisitRequest.joinedVisit(), followUpVisitRequest.joinedVisitorId(), currentUser);

        Visit visit = Visit.builder()
                .account(account)
                .visitDate(followUpVisitRequest.visitDate())
                .visitType(VisitType.FOLLOW_UP)
                .visitor(currentUser)
                .joinedVisit(followUpVisitRequest.joinedVisit())
                .joinedVisitor(joinedVisitor)
                .visitImpression(followUpVisitRequest.visitImpression())
                .materialsShared(followUpVisitRequest.materialsShared())
                .notes(followUpVisitRequest.notes())
                .nextVisitDate(followUpVisitRequest.nextVisitDate())
                .status(ApprovalStatus.ACTIVE)
                .build();
        Map<String, PhysicianFieldsRequest> uniquePhysicians = new LinkedHashMap<>();
        for(PhysicianFieldsRequest physician : followUpVisitRequest.physicians()){
            String key = physician.isExisting()?"id:" + physician.physicianId() : "npi:" + physician.npi();
            uniquePhysicians.putIfAbsent(key,physician);
        }
        List<VisitPhysician> links = new ArrayList<>();
        for(PhysicianFieldsRequest physicianReq : uniquePhysicians.values()) {
            Physician physician = physicianResolutionService.resolvePhysician(physicianReq, currentUser, ApprovalStatus.ACTIVE);
            links.add(VisitPhysician.builder().visit(visit).physician(physician).build());
            physicianResolutionService.linkAccountPhysician(account, physician, followUpVisitRequest.visitDate());
        }
        visit.setPhysicians(links);
        return visitRepository.save(visit);
    }

    public Account getAccountDetails(Long id){
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    public List<Account> getActiveAccounts(){
        return accountRepository.findAllByStatus(ApprovalStatus.ACTIVE);
    }

    public List<Physician> getActivePhysicians(){
        return physicianRepository.findAllByStatus(ApprovalStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<PhysicianSummaryResponse> findActivePhysiciansForAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        List<Physician> physicians = physicianRepository.findActivePhysiciansByAccountId(account.getId(),ApprovalStatus.ACTIVE);
        return physicians.stream()
                .map(p -> new PhysicianSummaryResponse(
                        p.getId(),
                        p.getNpi(),
                        p.getName(),
                        p.getSpecialty().getId(),
                        p.getSpecialty().getName(),
                        p.getEmail(),
                        p.getPhone()
                ))
                .toList();
    }
}
