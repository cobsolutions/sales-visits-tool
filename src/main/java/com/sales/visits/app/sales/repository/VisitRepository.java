package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit,Long> {
    Page<Visit> findByStatus(ApprovalStatus approvalStatus, Pageable pageable);
    Page<Visit> findByVisitorIdAndStatus(Long visitorId, ApprovalStatus approvalStatus, Pageable pageable);
    Page<Visit> findByVisitorIdInAndStatus(List<Long> visitorIds, ApprovalStatus approvalStatus, Pageable pageable);
    Page<Visit> findByVisitDateAndStatus(LocalDate visitDate,ApprovalStatus status, Pageable pageable);
    Page<Visit> findByVisitorUsernameAndStatus(String username,ApprovalStatus status,Pageable pageable);

    @Query("""
    SELECT v
    FROM Visit v
    JOIN v.account a
    WHERE LOWER(a.organizationName)
    LIKE LOWER(CONCAT('%', :organizationName, '%'))
    AND v.status = :status
    """)
    Page<Visit> findByOrganizationName(@Param("organizationName") String organizationName,@Param("status") ApprovalStatus status,Pageable pageable);

    @Query("""
    SELECT v
    FROM Visit v
    JOIN v.account a
    WHERE LOWER(a.organizationName)
    LIKE LOWER(CONCAT('%', :organizationName, '%'))
    AND v.status = :status
    AND v.visitor.id = :id
    """)
    Page<Visit> findByOrganizationNameAndStatusAndVisitorId(@Param("organizationName") String organizationName,@Param("status") ApprovalStatus status,@Param("id") Long userId,Pageable pageable);


    @Query("""
    SELECT v
    FROM Visit v
    JOIN v.account a
    WHERE v.visitDate = :visitDate
        AND v.status = :status
        AND v.visitor.id  = :id
    """)
    Page<Visit> findByVisitDateAndStatusAndVisitorId(@Param("visitDate") LocalDate visitDate,@Param("status") ApprovalStatus status,@Param("id") Long userId,Pageable pageable);
}
