package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.response.VisitReviewDetailResponse;
import com.sales.visits.app.sales.mapper.VisitReviewMapper;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.repository.UserRepository;
import com.sales.visits.app.sales.repository.VisitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Page<VisitReviewDetailResponse> findMyTeamActiveVisits(User teamLeader, Pageable pageable) {
        List<Long> salesRepIds = userRepository.findIdsByTeamLeaderId(teamLeader.getId());
        if (salesRepIds.isEmpty()) {
            return Page.empty(pageable);
        }
        salesRepIds.add(teamLeader.getId());
        return visitRepository.findByVisitorIdInAndStatus(salesRepIds,ApprovalStatus.ACTIVE,pageable)
                .map(visitReviewMapper::toDetail);
    }

    @Transactional
    public Page<VisitReviewDetailResponse> findAllSubmittedVisits(Pageable pageable) {
        return visitRepository.findByStatus(ApprovalStatus.ACTIVE,pageable)
                .map(visitReviewMapper::toDetail);
    }
}
