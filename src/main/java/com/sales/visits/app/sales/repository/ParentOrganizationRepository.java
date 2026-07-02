package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.ParentOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentOrganizationRepository extends JpaRepository<ParentOrganization, Long> {
}
