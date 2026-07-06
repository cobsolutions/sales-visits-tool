package com.sales.visits.app.sales.dto.response;

public record PhysicianSummaryResponse(
        Long id,
        String npi,
        String name,
        Long specialtyId,
        String specialtyName,
        String email,
        String phone
) {
}
