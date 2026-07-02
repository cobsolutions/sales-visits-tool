package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.PtocLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PtocLocationRepository extends JpaRepository<PtocLocation,Long> {
}
