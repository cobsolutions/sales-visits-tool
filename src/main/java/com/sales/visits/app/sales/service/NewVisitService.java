package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.request.NewAccountRequest;
import com.sales.visits.app.sales.dto.request.NewVisitRequest;
import com.sales.visits.app.sales.dto.request.PhysicianFieldsRequest;
import com.sales.visits.app.sales.exception.DuplicateNpiException;
import com.sales.visits.app.sales.exception.EntityNotFoundException;
import com.sales.visits.app.sales.model.entity.*;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.model.enums.UserStatus;
import com.sales.visits.app.sales.model.enums.VisitType;
import com.sales.visits.app.sales.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class NewVisitService {
    private final AccountRepository accountRepository;
    private final VisitRepository visitRepository;
    private final PhysicianRepository physicianRepository;
    private final AccountPhysicianRepository accountPhysicianRepository;
    private final BoroughRepository boroughRepository;
    private final PtocLocationRepository ptocLocationRepository;
    private final ParentOrganizationRepository parentOrganizationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserRepository userRepository;

    public NewVisitService(AccountRepository accountRepository, VisitRepository visitRepository, PhysicianRepository physicianRepository, AccountPhysicianRepository accountPhysicianRepository, BoroughRepository boroughRepository, PtocLocationRepository ptocLocationRepository, ParentOrganizationRepository parentOrganizationRepository, SpecialtyRepository specialtyRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.visitRepository = visitRepository;
        this.physicianRepository = physicianRepository;
        this.accountPhysicianRepository = accountPhysicianRepository;
        this.boroughRepository = boroughRepository;
        this.ptocLocationRepository = ptocLocationRepository;
        this.parentOrganizationRepository = parentOrganizationRepository;
        this.specialtyRepository = specialtyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Visit createNewVisit(NewVisitRequest newVisitRequest, User currentUser) {
        Account account = resolveAccount(newVisitRequest.account(), currentUser);
        User joinedVisitor = resolveJoinedVisitor(newVisitRequest.joinedVisit(), newVisitRequest.joinedVisitorId(), currentUser);
        Visit visit = Visit.builder()
                .account(account)
                .visitDate(newVisitRequest.visitDate())
                .visitType(VisitType.NEW)
                .visitor(currentUser)
                .joinedVisit(newVisitRequest.joinedVisit())
                .joinedVisitor(joinedVisitor)
                .visitImpression(newVisitRequest.visitImpression())
                .materialsShared(newVisitRequest.materialsShared())
                .notes(newVisitRequest.notes())
                .nextVisitDate(newVisitRequest.nextVisitDate())
                .status(ApprovalStatus.PENDING_APPROVAL)
                .build();
        Map<String,PhysicianFieldsRequest> uniquePhysicians = new LinkedHashMap<>();
        for(PhysicianFieldsRequest physician : newVisitRequest.physicians()){
            String key = physician.isExisting()?"id:" + physician.physicianId() : "npi:" + physician.npi();
            uniquePhysicians.putIfAbsent(key,physician);
        }
        List<VisitPhysician> links = new ArrayList<>();
        for(PhysicianFieldsRequest physicianReq : uniquePhysicians.values()){
            Physician physician = resolvePhysician(physicianReq, currentUser);
            links.add(VisitPhysician.builder().visit(visit).physician(physician).build());
            linkAccountPhysician(account, physician, newVisitRequest.visitDate());
        }
        visit.setPhysicians(links);
        return visitRepository.save(visit);
    }

    private User resolveJoinedVisitor(boolean joinedVisit,Long joinedVisitorId, User currentUser) {
        if (!joinedVisit) return null;
        if(joinedVisitorId == null) throw new IllegalArgumentException("joinedVisitorId is required when joinedVisit=true");

        if (joinedVisitorId.equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot select yourself as the joined visitor");
        }
        User joinedVisitor = userRepository.findById(joinedVisitorId)
                .orElseThrow(() -> new IllegalArgumentException("joinedVisitor not found"));
        if(joinedVisitor.getRole() != UserRole.SALES_REP && joinedVisitor.getRole() != UserRole.TEAM_LEADER){
            throw new IllegalArgumentException("joinedVisitor role can't be admin");
        }
        if (joinedVisitor.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("joinedVisitor must reference an active user");
        }
        return joinedVisitor;
    }

    private Account resolveAccount(NewAccountRequest accountRequest, User currentUser) {
        Optional<Account> existingAccount = accountRepository.findByOrganizationNameAndZipCodeAndAddressAndFloorSuite(
                accountRequest.organizationName(), accountRequest.zipCode(), accountRequest.address(), accountRequest.floorSuite()
        );
        if (existingAccount.isPresent()) return existingAccount.get();

        Borough borough = boroughRepository.findById(accountRequest.boroughId()).orElseThrow(() -> new EntityNotFoundException("Borough not found"));
        PtocLocation ptocLocation = ptocLocationRepository.findById(accountRequest.ptocLocationId()).orElseThrow(() -> new EntityNotFoundException("PtocLocation not found"));
        ParentOrganization parentOrganization = parentOrganizationRepository.findById(accountRequest.parentOrganizationId()).orElseThrow(() -> new EntityNotFoundException("ParentOrganization not found"));

        Account account = Account.builder()
                .organizationName(accountRequest.organizationName())
                .parentOrganization(parentOrganization)
                .organizationType(accountRequest.organizationType())
                .borough(borough)
                .ptocLocation(ptocLocation)
                .address(accountRequest.address())
                .floorSuite(accountRequest.floorSuite())
                .zipCode(accountRequest.zipCode())
                .phone(accountRequest.phone())
                .fax(accountRequest.fax())
                .email(accountRequest.email())
                .providesTelehealth(accountRequest.providesTelehealth())
                .sameDayWalkins(accountRequest.sameDayWalkins())
                .gatekeeperName(accountRequest.gatekeeperName())
                .gatekeeperTitle(accountRequest.gatekeeperTitle())
                .gatekeeperPhone(accountRequest.gatekeeperPhone())
                .gatekeeperEmail(accountRequest.gatekeeperEmail())
                .preferredCommunication(accountRequest.preferredCommunication())
                .status(ApprovalStatus.PENDING_APPROVAL)
                .submittedBy(currentUser)
                .build();
        try {
            return accountRepository.save(account);
        } catch (DataIntegrityViolationException e) {
            return accountRepository
                    .findByOrganizationNameAndZipCodeAndAddressAndFloorSuite(
                            accountRequest.organizationName(), accountRequest.zipCode(), accountRequest.address(), accountRequest.floorSuite())
                    .orElseThrow(() -> e);
        }
    }

    private Physician resolvePhysician(PhysicianFieldsRequest physicianFieldsRequest, User currentUser) {
        if (physicianFieldsRequest.isExisting()) {
            return physicianRepository.findById(physicianFieldsRequest.physicianId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Physician " + physicianFieldsRequest.physicianId() + " not found"));
        }
        return physicianRepository.findByNpi(physicianFieldsRequest.npi())
                .orElseGet(() -> {
                    Specialty specialty = specialtyRepository.findById(physicianFieldsRequest.specialtyId())
                            .orElseThrow(() -> new EntityNotFoundException("Specialty not found"));
                    try {
                        return physicianRepository.save(Physician.builder()
                                .npi(physicianFieldsRequest.npi())
                                .name(physicianFieldsRequest.name())
                                .specialty(specialty)
                                .email(physicianFieldsRequest.email())
                                .phone(physicianFieldsRequest.phone())
                                .status(ApprovalStatus.PENDING_APPROVAL)
                                .submittedBy(currentUser)
                                .build());
                    } catch (DataIntegrityViolationException e) {
                        throw new DuplicateNpiException(physicianFieldsRequest.npi());
                    }
                });
    }

    private void linkAccountPhysician(Account account, Physician physician, LocalDate dateFirstSeen) {
        boolean alreadyLinked = accountPhysicianRepository
                .existsByAccountIdAndPhysicianId(account.getId(), physician.getId());
        if (!alreadyLinked) {
            accountPhysicianRepository.save(AccountPhysician.builder()
                    .account(account)
                    .physician(physician)
                    .dateFirstSeen(dateFirstSeen)
                    .build());
        }
    }
}
