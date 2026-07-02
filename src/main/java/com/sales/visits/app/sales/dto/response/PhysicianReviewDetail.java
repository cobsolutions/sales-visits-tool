package com.sales.visits.app.sales.dto.response;

import com.sales.visits.app.sales.model.enums.ApprovalStatus;

public record PhysicianReviewDetail(
        Long id,
        ApprovalStatus status,
        String npi,
        String name,
        Long specialtyId,
        String email,
        String phone
) {}
