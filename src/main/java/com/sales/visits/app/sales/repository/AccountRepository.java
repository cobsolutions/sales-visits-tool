package com.sales.visits.app.sales.repository;

import com.sales.visits.app.sales.model.entity.Account;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByOrganizationNameAndZipCodeAndAddressAndFloorSuite(
            String organizationName, String zipCode, String address, String floorSuite);

    List<Account> findAllByStatus(ApprovalStatus status);
}
