package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Physician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhysicianRepository extends JpaRepository<Physician,Long> {
    Optional<Physician> findByNpi(String npi);
}
