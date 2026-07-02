package com.sales.visits.app.sales.dto.request;

import jakarta.validation.constraints.*;

public record PhysicianFieldsRequest(
        Long physicianId,

        @Pattern(regexp = "^\\d{10}$", message = "NPI must be exactly 10 digits") String npi,

        @NotBlank String name,

        @NotNull Long specialtyId,

        @Email String email,

        String phone
) {
    public boolean isExisting() { return physicianId != null; }

    @AssertTrue(message = "Provide either physicianId, or npi+name+specialtyId for a new physician")
    public boolean isValid() {
        return physicianId != null || npi != null;
    }
}
