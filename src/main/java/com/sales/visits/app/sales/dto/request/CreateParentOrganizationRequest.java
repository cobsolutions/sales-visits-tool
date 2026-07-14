package com.sales.visits.app.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateParentOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        @Size(max = 150, message = "Organization name must be 150 characters or fewer")
        String name
) {
}
