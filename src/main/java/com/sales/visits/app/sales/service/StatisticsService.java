package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.response.VisitReviewDetailResponse;
import com.sales.visits.app.sales.mapper.VisitReviewMapper;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.UserRole;
import com.sales.visits.app.sales.repository.UserRepository;
import com.sales.visits.app.sales.repository.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsService {
    private final VisitRepository visitRepository;
    private final VisitReviewMapper visitReviewMapper;
    private final UserRepository userRepository;

    public StatisticsService(VisitRepository visitRepository, VisitReviewMapper visitReviewMapper, UserRepository userRepository) {
        this.visitRepository = visitRepository;
        this.visitReviewMapper = visitReviewMapper;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<VisitReviewDetailResponse> findMyActiveVisits(User currentUser, Pageable pageable) {
        return visitRepository.findByVisitorIdAndStatus(currentUser.getId(), ApprovalStatus.ACTIVE, pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional(readOnly = true)
    public Page<VisitReviewDetailResponse> findMyPendingVisits(User currentUser, Pageable pageable) {
        return visitRepository.findByVisitorIdAndStatus(currentUser.getId(), ApprovalStatus.PENDING_APPROVAL, pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findMyTeamActiveVisits(User teamLeader, Pageable pageable) {
        List<Long> salesRepIds = userRepository.findIdsByTeamLeaderId(teamLeader.getId());
        salesRepIds.add(teamLeader.getId());
        return visitRepository.findByVisitorIdInAndStatus(salesRepIds,ApprovalStatus.ACTIVE,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findMyTeamPendingVisits(User teamLeader, Pageable pageable) {
        List<Long> salesRepIds = userRepository.findIdsByTeamLeaderId(teamLeader.getId());
        salesRepIds.add(teamLeader.getId());
        return visitRepository.findByVisitorIdInAndStatus(salesRepIds,ApprovalStatus.PENDING_APPROVAL,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findAllSubmittedVisits(Pageable pageable) {
        return visitRepository.findByStatus(ApprovalStatus.ACTIVE,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findAllPendingVisits(Pageable pageable) {
        return visitRepository.findByStatus(ApprovalStatus.PENDING_APPROVAL,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findByVisitDate(LocalDate date,Pageable pageable) {
        return visitRepository.findByVisitDateAndStatus(date,ApprovalStatus.ACTIVE,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findByVisitDateAndVisitorId(LocalDate visitDate,Long userId,Pageable pageable){
        return visitRepository.findByVisitDateAndStatusAndVisitorId(visitDate,ApprovalStatus.ACTIVE,userId,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findByOrganizationNameAndVisitorId(String organizationName, Long userId,Pageable pageable){
        return visitRepository.findByOrganizationNameAndStatusAndVisitorId(organizationName,ApprovalStatus.ACTIVE,userId,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findByOrganizationName(String organizationName,Pageable pageable) {
        return visitRepository.findByOrganizationName(organizationName,ApprovalStatus.ACTIVE,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findByVisitorUsername(String username, Pageable pageable) {
        return visitRepository.findByVisitorUsernameAndStatus(username,ApprovalStatus.ACTIVE,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public List<VisitReviewDetailResponse> findAllForExport(User currentUser, String filterType, String filterValue) {

        Pageable unpaged = Pageable.unpaged();
        boolean isSalesRep = currentUser.getRole() == UserRole.SALES_REP;
        boolean isTeamLeader = currentUser.getRole() == UserRole.TEAM_LEADER;
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        Page<VisitReviewDetailResponse> page = null;

        if (filterType == null || filterValue == null || filterValue.isBlank()) {
            if (isSalesRep) {
                page = findMyActiveVisits(currentUser, unpaged);
            } else if (isTeamLeader) {
                page = findMyTeamActiveVisits(currentUser, unpaged);
            } else if(isAdmin) {
                page = findAllSubmittedVisits(unpaged);
            }
        } else {
            switch (filterType) {
                case "date" -> {
                    LocalDate date = LocalDate.parse(filterValue);
                    page = isSalesRep
                            ? findByVisitDateAndVisitorId(date, currentUser.getId(), unpaged)
                            : findByVisitDate(date, unpaged);
                }
                case "org" -> page = isSalesRep
                        ? findByOrganizationNameAndVisitorId(filterValue, currentUser.getId(), unpaged)
                        : findByOrganizationName(filterValue, unpaged);
                case "name" -> {
                    if (isSalesRep) {
                        throw new AccessDeniedException("Sales reps cannot filter by submitted-by");
                    }
                    page = findByVisitorUsername(filterValue, unpaged);
                }
                default -> throw new IllegalArgumentException("Invalid filter type: " + filterType);
            }
        }

        return page.getContent();
    }
}
