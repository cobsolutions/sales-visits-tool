package com.sales.visits.app.sales.dto.request;

import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.VisitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalendarVisitDto {
    private Long id;
    private String accountName;
    private String visitorName;
    private Long visitorId;
    private LocalDate visitDate;
    private LocalDate nextVisitDate;
    private VisitType visitType;
    private ApprovalStatus status;
}
