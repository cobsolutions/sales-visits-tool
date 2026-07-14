package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.ParentOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentOrganizationRepository extends JpaRepository<ParentOrganization, Long> {
    Optional<ParentOrganization> findByName(String name);
    List<ParentOrganization> findAllByOrderByNameAsc();
}
