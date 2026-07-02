package com.sales.visits.app.sales.dto.response;

import com.sales.visits.app.sales.model.enums.UserRole;

public record UserSummaryResponse(
        String username,
        UserRole role
) {}
