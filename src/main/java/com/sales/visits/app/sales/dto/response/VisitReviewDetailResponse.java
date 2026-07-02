package com.sales.visits.app.sales.dto.response;

import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.VisitImpression;
import com.sales.visits.app.sales.model.enums.VisitType;

import java.time.LocalDate;
import java.util.List;

public record VisitReviewDetailResponse(
        Long visitId,
        ApprovalStatus visitStatus,
        LocalDate visitDate,
        VisitType visitType,
        VisitImpression visitImpression,
        boolean joinedVisit,
        Long joinedVisitorId,
        List<String> materialsShared,
        String notes,
        LocalDate nextVisitDate,
        AccountReviewDetail account,
        List<PhysicianReviewDetail> physicians
) {}
