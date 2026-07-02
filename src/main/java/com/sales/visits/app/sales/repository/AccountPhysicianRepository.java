package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.AccountPhysician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountPhysicianRepository extends JpaRepository<AccountPhysician,Long> {
    boolean existsByAccountIdAndPhysicianId(Long accountId,Long physicianId);
}
