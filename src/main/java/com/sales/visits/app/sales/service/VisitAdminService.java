package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.request.*;
import com.sales.visits.app.sales.exception.DuplicateNpiException;
import com.sales.visits.app.sales.exception.EntityNotFoundException;
import com.sales.visits.app.sales.model.entity.*;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VisitAdminService {
    private final VisitRepository visitRepository;
    private final PhysicianRepository physicianRepository;
    private final AccountPhysicianRepository accountPhysicianRepository;
    private final BoroughRepository boroughRepository;
    private final PtocLocationRepository ptocLocationRepository;
    private final ParentOrganizationRepository parentOrganizationRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserRepository userRepository;

    public VisitAdminService(VisitRepository visitRepository, PhysicianRepository physicianRepository, AccountPhysicianRepository accountPhysicianRepository, BoroughRepository boroughRepository, PtocLocationRepository ptocLocationRepository, ParentOrganizationRepository parentOrganizationRepository, SpecialtyRepository specialtyRepository, UserRepository userRepository) {
        this.visitRepository = visitRepository;
        this.physicianRepository = physicianRepository;
        this.accountPhysicianRepository = accountPhysicianRepository;
        this.boroughRepository = boroughRepository;
        this.ptocLocationRepository = ptocLocationRepository;
        this.parentOrganizationRepository = parentOrganizationRepository;
        this.specialtyRepository = specialtyRepository;
        this.userRepository = userRepository;
    }

    public Page<Visit> findPending(Pageable pageable) {
        return visitRepository.findByStatus(ApprovalStatus.PENDING_APPROVAL, pageable);
    }

    public Visit getById(Long id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Visit not found"));
    }

    @Transactional
    public Visit reviewAndApprove(Long visitId, VisitReviewRequest visitReviewRequest, User admin) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Visit not found"));
        if(visit.getStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("Visit status must be PENDING_APPROVAL");
        }

        //update visit
        updateVisitFields(visit,visitReviewRequest.visit());
        //update account
        updateAccountFields(visit.getAccount(),visitReviewRequest.account(),admin);
        //update physician
        updatePhysicianFields(visit,visitReviewRequest.physicians(),admin);

        visit.setStatus(ApprovalStatus.ACTIVE);
        visit.setApprovedBy(admin.getId());
        visit.setReviewedAt(OffsetDateTime.now());
        return visitRepository.save(visit);
    }

    @Transactional
    public Visit reject(Long visitId, RejectRequest reason,User admin) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("Visit not found"));

        if (visit.getStatus() != ApprovalStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Visit is not pending approval");
        }

        visit.setStatus(ApprovalStatus.REJECTED);
        visit.setRejectionReason(reason.reason());
        visit.setApprovedBy(admin.getId());
        visit.setReviewedAt(OffsetDateTime.now());
        return visit;
    }

    public void updateVisitFields(Visit visit, VisitFieldsRequest visitFieldsRequest) {
        visit.setVisitDate(visitFieldsRequest.visitDate());
        visit.setVisitImpression(visitFieldsRequest.visitImpression());
        visit.setMaterialsShared(visitFieldsRequest.materialsShared());
        visit.setNotes(visitFieldsRequest.notes());
        visit.setNextVisitDate(visitFieldsRequest.nextVisitDate());
        visit.setJoinedVisit(visitFieldsRequest.joinedVisit());

        if (visitFieldsRequest.joinedVisit()) {
            if (visitFieldsRequest.joinedVisitorId() == null) {
                throw new IllegalArgumentException("joinedVisitorId is required when joinedVisit=true");
            }
            if (visitFieldsRequest.joinedVisitorId().equals(visit.getVisitor().getId())) {
                throw new IllegalArgumentException("Joined visitor cannot be the same as the visit submitter");
            }

            User joinedVisitor = userRepository.findById(visitFieldsRequest.joinedVisitorId())
                    .orElseThrow(() -> new EntityNotFoundException("Joined visitor not found"));

            if (joinedVisitor.getRole() != UserRole.SALES_REP && joinedVisitor.getRole() != UserRole.TEAM_LEADER) {
                throw new IllegalArgumentException(
                        "joinedVisitor role must be a SALES_REP or TEAM_LEADER user");
            }

            visit.setJoinedVisitor(joinedVisitor);
        } else {
            visit.setJoinedVisitor(null);
        }
    }

    public void updateAccountFields(Account account, NewAccountRequest newAccountRequest,User admin) {
        if (account.getStatus() == ApprovalStatus.ACTIVE) {
            return;
        }

        account.setOrganizationName(newAccountRequest.organizationName());
        account.setOrganizationType(newAccountRequest.organizationType());
        account.setAddress(newAccountRequest.address());
        account.setFloorSuite(newAccountRequest.floorSuite());
        account.setZipCode(newAccountRequest.zipCode());
        account.setPhone(newAccountRequest.phone());
        account.setFax(newAccountRequest.fax());
        account.setEmail(newAccountRequest.email());
        account.setProvidesTelehealth(newAccountRequest.providesTelehealth());
        account.setSameDayWalkins(newAccountRequest.sameDayWalkins());
        account.setGatekeeperName(newAccountRequest.gatekeeperName());
        account.setGatekeeperTitle(newAccountRequest.gatekeeperTitle());
        account.setGatekeeperPhone(newAccountRequest.gatekeeperPhone());
        account.setGatekeeperEmail(newAccountRequest.gatekeeperEmail());
        account.setPreferredCommunication(newAccountRequest.preferredCommunication());

        account.setBorough(boroughRepository.findById(newAccountRequest.boroughId())
                .orElseThrow(() -> new EntityNotFoundException("Borough not found")));
        account.setPtocLocation(ptocLocationRepository.findById(newAccountRequest.ptocLocationId())
                .orElseThrow(() -> new EntityNotFoundException("PTOC location not found")));

        if (newAccountRequest.parentOrganizationId() != null) {
            account.setParentOrganization(parentOrganizationRepository.findById(newAccountRequest.parentOrganizationId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent organization not found")));
        }

        account.setStatus(ApprovalStatus.ACTIVE);
        account.setApprovedBy(admin);
    }

    public void updatePhysicianFields(Visit visit, List<PhysicianFieldsRequest> physicianFieldsRequests, User admin) {

        Map<Long, VisitPhysician> currentLinks = visit.getPhysicians().stream()
                .collect(Collectors.toMap(vp -> vp.getPhysician().getId(), vp -> vp));

        Set<Long> requestedIds = new HashSet<>();
        for (PhysicianFieldsRequest physician : physicianFieldsRequests) {
            if (!physician.isExisting()) {
                throw new IllegalArgumentException(
                        "This operation only updates existing physicians; physicianId is required");
            }
            if (!currentLinks.containsKey(physician.physicianId())) {
                throw new IllegalArgumentException(
                        "Physician " + physician.physicianId() + " is not linked to this visit");
            }
            requestedIds.add(physician.physicianId());
        }

        if (!requestedIds.equals(currentLinks.keySet())) {
            throw new IllegalArgumentException(
                    "All physicians linked to this visit must be included in the update request");
        }

        for (PhysicianFieldsRequest physicianField : physicianFieldsRequests) {
            Physician physician = currentLinks.get(physicianField.physicianId()).getPhysician();

            if (physician.getStatus() == ApprovalStatus.ACTIVE) {
                continue;
            }

            if (physicianField.npi() != null) {
                isNpiUnique(physicianField.npi(), physician.getId());
                physician.setNpi(physicianField.npi());
            }
            physician.setName(physicianField.name());
            physician.setEmail(physicianField.email());
            physician.setPhone(physicianField.phone());
            physician.setSpecialty(specialtyRepository.findById(physicianField.specialtyId())
                    .orElseThrow(() -> new EntityNotFoundException("Specialty not found")));
            physician.setStatus(ApprovalStatus.ACTIVE);
            physician.setApprovedBy(admin);
        }
    }

    private Physician resolvePhysician(PhysicianFieldsRequest physicianFieldsRequest, User admin) {
        if(physicianFieldsRequest.isExisting()){
            return physicianRepository.findById(physicianFieldsRequest.physicianId())
                    .orElseThrow(() -> new EntityNotFoundException("Physician with id: " + physicianFieldsRequest.physicianId() + " not found"));
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
                                .status(ApprovalStatus.ACTIVE)
                                .submittedBy(admin)
                                .approvedBy(admin)
                                .build());
                    } catch (DataIntegrityViolationException e) {
                        throw new DuplicateNpiException(physicianFieldsRequest.npi());
                    }
                });
    }

    private void isNpiUnique(String npi, Long currentPhysicianId) {
        physicianRepository.findByNpi(npi)
                .filter(other -> !other.getId().equals(currentPhysicianId))
                .ifPresent(other -> { throw new DuplicateNpiException(npi); });
    }

    private void linkAccountPhysician(Account account, Physician physician, java.time.LocalDate dateFirstSeen) {
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
