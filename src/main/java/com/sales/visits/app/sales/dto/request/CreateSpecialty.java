package com.sales.visits.app.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSpecialty(
        @NotBlank(message = "Specialty is required")
        @Size(max = 100, message = "Specialty must be 100 characters or fewer")
        String name
) {
}
