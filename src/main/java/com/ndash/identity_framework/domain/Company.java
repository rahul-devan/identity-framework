package com.ndash.identity_framework.domain;

import com.ndash.identity_framework.domain.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Data
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

//    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
//    private CompanyContact contact;


    @ManyToOne
    @JoinColumn(name = "primary_contact_user_id")
    private User primaryContact;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
}
