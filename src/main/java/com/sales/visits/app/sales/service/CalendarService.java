package com.sales.visits.app.sales.service;


import com.sales.visits.app.sales.dto.request.CalendarVisitDto;
import com.sales.visits.app.sales.model.entity.User;
import com.sales.visits.app.sales.model.entity.Visit;
import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.repository.VisitRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
public class CalendarService {
    private final VisitRepository visitRepository;

    public CalendarService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    private static final ApprovalStatus CALENDAR_STATUS = ApprovalStatus.ACTIVE;

    @Transactional(readOnly = true)
    public List<CalendarVisitDto> getUpcomingVisits(User currentUser) {
        return switch (currentUser.getRole()) {
            case SALES_REP -> getUpcomingVisitsForSalesRep(currentUser);
            case TEAM_LEADER -> getUpcomingVisitsForTeamLeader(currentUser);
            case ADMIN -> getUpcomingVisitsForAdmin(currentUser);
            default -> throw new AccessDeniedException(
                    "Calendar is not available for role " + currentUser.getRole());
        };
    }

    @Transactional(readOnly = true)
    public List<CalendarVisitDto> getUpcomingVisitsForSalesRep(User salesRep) {
        List<Visit> visits = visitRepository.findUpcomingForSales(salesRep.getId(), CALENDAR_STATUS, LocalDate.now());
        return toDtoList(visits);
    }

    @Transactional(readOnly = true)
    public List<CalendarVisitDto> getUpcomingVisitsForTeamLeader(User teamLeader) {
        List<Visit> visits = visitRepository.findUpcomingForTeam(teamLeader.getId(), CALENDAR_STATUS, LocalDate.now());
        return toDtoList(visits);
    }

    @Transactional(readOnly = true)
    public List<CalendarVisitDto> getUpcomingVisitsForAdmin(User admin) {
        List<Visit> visits = visitRepository.findUpcomingAll(CALENDAR_STATUS, LocalDate.now());
        return toDtoList(visits);
    }

    private List<CalendarVisitDto> toDtoList(List<Visit> visits) {
        return visits.stream().map(this::toDto).toList();
    }

    private CalendarVisitDto toDto(Visit v) {
        return CalendarVisitDto.builder()
                .id(v.getId())
                .accountName(v.getAccount().getOrganizationName())
                .visitorName(v.getVisitor().getUsername())
                .visitorId(v.getVisitor().getId())
                .visitDate(v.getVisitDate())
                .nextVisitDate(v.getNextVisitDate())
                .visitType(v.getVisitType())
                .status(v.getStatus())
                .build();
    }
}
