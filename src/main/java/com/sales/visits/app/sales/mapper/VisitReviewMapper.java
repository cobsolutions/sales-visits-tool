package com.sales.visits.app.sales.mapper;

import com.sales.visits.app.sales.dto.response.AccountReviewDetail;
import com.sales.visits.app.sales.dto.response.PhysicianReviewDetail;
import com.sales.visits.app.sales.dto.response.VisitReviewDetailResponse;
import com.sales.visits.app.sales.model.entity.Account;
import com.sales.visits.app.sales.model.entity.Physician;
import com.sales.visits.app.sales.model.entity.Visit;
import org.springframework.stereotype.Component;

@Component
public class VisitReviewMapper {
    public VisitReviewDetailResponse toDetail(Visit v) {
        return new VisitReviewDetailResponse(
                v.getId(), v.getStatus(), v.getVisitDate(), v.getVisitType(),
                v.getVisitImpression(), v.isJoinedVisit(),
                v.getJoinedVisitor() != null ? v.getJoinedVisitor().getId() : null,
                v.getMaterialsShared(), v.getNotes(), v.getNextVisitDate(),
                toAccountDetail(v.getAccount()),
                v.getPhysicians().stream().map(vp -> toPhysicianDetail(vp.getPhysician())).toList(),
                v.getRejectionReason()
        );
    }

    private AccountReviewDetail toAccountDetail(Account a) {
        return new AccountReviewDetail(
                a.getId(), a.getStatus(), a.getOrganizationName(),
                a.getParentOrganization() != null ? a.getParentOrganization().getId() : null,
                a.getOrganizationType(), a.getBorough().getId(), a.getPtocLocation().getId(),
                a.getAddress(), a.getFloorSuite(), a.getZipCode(), a.getPhone(), a.getFax(),
                a.getEmail(), a.isProvidesTelehealth(), a.isSameDayWalkins(),
                a.getGatekeeperName(), a.getGatekeeperTitle(), a.getGatekeeperPhone(),
                a.getGatekeeperEmail(), a.getPreferredCommunication()
        );
    }

    private PhysicianReviewDetail toPhysicianDetail(Physician p) {
        return new PhysicianReviewDetail(
                p.getId(), p.getStatus(), p.getNpi(), p.getName(),
                p.getSpecialty().getId(), p.getEmail(), p.getPhone()
        );
    }
}
