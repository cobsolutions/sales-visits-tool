package com.sales.visits.app.sales.dto.response;

import com.sales.visits.app.sales.model.enums.UserRole;

public record UserSummaryResponse(
        long id,
        String username,
        UserRole role
) {}
