package com.sales.visits.app.sales.dto.response;

import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.VisitImpression;
import com.sales.visits.app.sales.model.enums.VisitType;

import java.time.LocalDate;
import java.util.List;

public record VisitResponse(
        Long id,
        ApprovalStatus status,
        Long accountId,
        LocalDate visitDate,
        VisitType visitType,
        Long visitorId,
        boolean joinedVisit,
        Long joinedVisitorId,
        VisitImpression visitImpression,
        List<String> materialsShared,
        String notes,
        LocalDate nextVisitDate,
        List<Long> physicianIds
) {}
