package com.sales.visits.app.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBorough(
        @NotBlank(message = "Borough is required")
        @Size(max = 100, message = "Borough must be 100 characters or fewer")
        String name
) {
}
