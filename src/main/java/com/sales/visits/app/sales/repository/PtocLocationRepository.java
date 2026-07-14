package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.PtocLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PtocLocationRepository extends JpaRepository<PtocLocation,Long> {
    Optional<PtocLocation> findByName(String name);
    List<PtocLocation> findAllByOrderByNameAsc();
}
