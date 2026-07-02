package com.sales.visits.app.sales.dto.response;

import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.PreferredCommunication;

public record AccountReviewDetail(
        Long id,
        ApprovalStatus status,
        String organizationName,
        Long parentOrganizationId,
        String organizationType,
        Long boroughId,
        Long ptocLocationId,
        String address,
        String floorSuite,
        String zipCode,
        String phone,
        String fax,
        String email,
        boolean providesTelehealth,
        boolean sameDayWalkins,
        String gatekeeperName,
        String gatekeeperTitle,
        String gatekeeperPhone,
        String gatekeeperEmail,
        PreferredCommunication preferredCommunication
) {}
