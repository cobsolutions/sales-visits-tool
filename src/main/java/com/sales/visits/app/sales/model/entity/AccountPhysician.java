package com.sales.visits.app.sales.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(
        name = "account_physicians",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_account_physician",
                        columnNames = {"account_id", "physician_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountPhysician {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "physician_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Physician physician;

    @Column(name = "date_first_seen", nullable = false)
    private LocalDate dateFirstSeen = LocalDate.now();

}
