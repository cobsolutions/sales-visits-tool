package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit,Long> {
    Page<Visit> findByStatus(ApprovalStatus approvalStatus, Pageable pageable);
    Page<Visit> findByVisitorIdAndStatus(Long visitorId, ApprovalStatus approvalStatus, Pageable pageable);
    Page<Visit> findByVisitorIdInAndStatus(List<Long> visitorIds, ApprovalStatus approvalStatus, Pageable pageable);
}
