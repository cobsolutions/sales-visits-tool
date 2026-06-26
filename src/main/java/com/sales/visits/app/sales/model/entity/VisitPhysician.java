package com.sales.visits.app.sales.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "visit_physicians",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_visit_physician",
                        columnNames = {"visit_id", "physician_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitPhysician {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physician_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Physician physician;
}
