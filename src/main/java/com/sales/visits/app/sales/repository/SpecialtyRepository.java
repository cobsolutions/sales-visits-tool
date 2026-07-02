package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty,Long> {
}
