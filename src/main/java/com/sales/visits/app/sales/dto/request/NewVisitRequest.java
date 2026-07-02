package com.sales.visits.app.sales.dto.request;

import com.sales.visits.app.sales.model.enums.VisitImpression;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record NewVisitRequest(
        @Valid @NotNull NewAccountRequest account,
        @NotNull LocalDate visitDate,
        @NotNull VisitImpression visitImpression,
        boolean joinedVisit,
        Long joinedVisitorId,
        List<String> materialsShared,
        String notes,
        LocalDate nextVisitDate,

        @NotEmpty
        @Size(max = 20, message = "A visit can include at most 20 physicians")
        @Valid
        List<PhysicianFieldsRequest> physicians
) {
}
