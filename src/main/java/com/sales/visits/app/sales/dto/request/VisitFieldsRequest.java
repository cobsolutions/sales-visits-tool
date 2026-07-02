package com.sales.visits.app.sales.dto.request;

import com.sales.visits.app.sales.model.enums.VisitImpression;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.util.List;

public record VisitFieldsRequest(
        @NotNull LocalDate visitDate,

        @NotNull VisitImpression visitImpression,

        boolean joinedVisit,

        Long joinedVisitorId,

        List<String> materialsShared,

        String notes,

        LocalDate nextVisitDate
) {}
