package com.sales.visits.app.sales.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(@NotBlank(message = "A rejection reason is required") String reason) {}
