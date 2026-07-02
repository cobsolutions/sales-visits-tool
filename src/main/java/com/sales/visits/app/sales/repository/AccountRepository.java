package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByOrganizationNameAndZipCodeAndAddressAndFloorSuite(
            String organizationName, String zipCode, String address, String floorSuite);
}
