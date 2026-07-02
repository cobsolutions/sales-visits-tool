package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends JpaRepository<Visit,Long> {
    Page<Visit> findByStatus(ApprovalStatus approvalStatus, Pageable pageable);
}
