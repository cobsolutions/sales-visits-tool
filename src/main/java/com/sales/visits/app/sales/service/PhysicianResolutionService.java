package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.request.PhysicianFieldsRequest;
import com.sales.visits.app.sales.exception.DuplicateNpiException;
import com.sales.visits.app.sales.exception.EntityNotFoundException;
import com.sales.visits.app.sales.model.entity.*;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.repository.AccountPhysicianRepository;
import com.sales.visits.app.sales.repository.PhysicianRepository;
import com.sales.visits.app.sales.repository.SpecialtyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PhysicianResolutionService {
    private final PhysicianRepository physicianRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AccountPhysicianRepository accountPhysicianRepository;

    public PhysicianResolutionService(PhysicianRepository physicianRepository, SpecialtyRepository specialtyRepository, AccountPhysicianRepository accountPhysicianRepository) {
        this.physicianRepository = physicianRepository;
        this.specialtyRepository = specialtyRepository;
        this.accountPhysicianRepository = accountPhysicianRepository;
    }

    public Physician resolvePhysician(PhysicianFieldsRequest physicianFieldsRequest, User currentUser,ApprovalStatus approvalStatus) {
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
                                .status(approvalStatus)
                                .submittedBy(currentUser)
                                .build());
                    } catch (DataIntegrityViolationException e) {
                        throw new DuplicateNpiException(physicianFieldsRequest.npi());
                    }
                });
    }

    public void linkAccountPhysician(Account account, Physician physician, LocalDate dateFirstSeen) {
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

    public boolean existsByNpi(String npi) {
        return physicianRepository.existsByNpi(npi);
    }
}
