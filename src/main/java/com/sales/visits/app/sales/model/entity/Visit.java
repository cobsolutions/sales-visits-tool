package com.sales.visits.app.sales.model.entity;

import com.sales.visits.app.sales.model.enums.VisitImpression;
import com.sales.visits.app.sales.model.enums.VisitType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "visits",
        check = {
                @CheckConstraint(
                        name = "chk_joined_visitor",
                        constraint = "(joined_visit = TRUE AND joined_visitor_id IS NOT NULL) " +
                                "OR (joined_visit = FALSE AND joined_visitor_id IS NULL)"
                )
        }
)
@Getter
@Setter
@ToString(exclude = {"account", "visitor", "joinedVisitor", "physicians"})
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "visit_type", nullable = false)
    private VisitType visitType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visitor_id", nullable = false)
    private User visitor;

    @Builder.Default
    @Column(name = "joined_visit", nullable = false)
    private boolean joinedVisit = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "joined_visitor_id")
    private User joinedVisitor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "visit_impression", nullable = false)
    private VisitImpression visitImpression;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "materials_shared")
    private List<String> materialsShared;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VisitPhysician> physicians = new ArrayList<>();

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING_APPROVAL";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.status == null) {
            this.status = "RECORDED";
        }
    }
}
