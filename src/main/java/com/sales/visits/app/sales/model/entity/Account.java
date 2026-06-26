package com.sales.visits.app.sales.model.entity;

import com.sales.visits.app.sales.model.enums.ApprovalStatus;
import com.sales.visits.app.sales.model.enums.PreferredCommunication;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_account_identity",
                        columnNames = {"organization_name", "zip_code", "address", "floor_suite"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", unique = true, length = 30)
    private String accountId;

    @Column(name = "organization_name", nullable = false, length = 255)
    private String organizationName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_organization_id")
    private ParentOrganization parentOrganization;

    @Column(name = "organization_type", length = 100)
    private String organizationType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borough_id", nullable = false)
    private Borough borough;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ptoc_location_id", nullable = false)
    private PtocLocation ptocLocation;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "floor_suite", length = 50)
    private String floorSuite;

    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "fax", length = 20)
    private String fax;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "provides_telehealth", nullable = false)
    private boolean providesTelehealth = false;

    @Column(name = "same_day_walkins", nullable = false)
    private boolean sameDayWalkins = false;

    @Column(name = "gatekeeper_name", length = 150)
    private String gatekeeperName;

    @Column(name = "gatekeeper_title", length = 100)
    private String gatekeeperTitle;

    @Column(name = "gatekeeper_phone", length = 20)
    private String gatekeeperPhone;

    @Column(name = "gatekeeper_email", length = 150)
    private String gatekeeperEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_communication")
    private PreferredCommunication preferredCommunication;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING_APPROVAL;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ApprovalStatus.PENDING_APPROVAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
