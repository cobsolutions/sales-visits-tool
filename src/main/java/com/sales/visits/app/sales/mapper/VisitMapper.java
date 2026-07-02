package com.sales.visits.app.sales.mapper;

import com.sales.visits.app.sales.dto.response.VisitResponse;
import com.sales.visits.app.sales.model.entity.Visit;
import org.springframework.stereotype.Component;

@Component
public class VisitMapper {
    public VisitResponse toResponse(Visit v) {
        return new VisitResponse(
                v.getId(), v.getStatus(), v.getAccount().getId(), v.getVisitDate(), v.getVisitType(),
                v.getVisitor().getId(), v.isJoinedVisit(),
                v.getJoinedVisitor() != null ? v.getJoinedVisitor().getId() : null,
                v.getVisitImpression(), v.getMaterialsShared(), v.getNotes(), v.getNextVisitDate(),
                v.getPhysicians().stream().map(vp -> vp.getPhysician().getId()).toList()
        );
    }
}
