package com.sales.visits.app.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VisitReviewRequest(
        @Valid @NotNull VisitFieldsRequest visit,

        @Valid @NotNull AccountFieldsRequest account,

        @Valid @NotEmpty
        @Size(max = 20, message = "A visit can include at most 20 physicians")
        List<PhysicianFieldsRequest> physicians
) {}
