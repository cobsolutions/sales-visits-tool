package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Physician;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicianRepository extends JpaRepository<Physician,Long> {
    Optional<Physician> findByNpi(String npi);
    List<Physician> findAllByStatus(ApprovalStatus  status);
    boolean existsByNpi(String npi);

    @Query("""
       SELECT ap.physician FROM AccountPhysician ap
       WHERE ap.account.id = :accountId
         AND ap.physician.status = :status
       ORDER BY ap.physician.name
       """)
    List<Physician> findActivePhysiciansByAccountId(
            @Param("accountId") Long accountId,
            @Param("status") ApprovalStatus status
    );
}
