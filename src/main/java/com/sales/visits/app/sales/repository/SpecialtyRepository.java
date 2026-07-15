package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty,Long> {
    Optional<Specialty> findByName(String name);
    List<Specialty> findAllByOrderByNameAsc();
}
