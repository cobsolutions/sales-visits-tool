package com.sales.visits.app.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePtocLocation(
        @NotBlank(message = "Ptoc location is required")
        @Size(max = 100, message = "Ptoc location must be 100 characters or fewer")
        String name
) {
}
