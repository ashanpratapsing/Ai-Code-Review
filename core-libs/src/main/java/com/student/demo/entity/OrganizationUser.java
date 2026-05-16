package com.student.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "organization_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OrganizationUser.OrganizationUserId.class)
public class OrganizationUser {

    @Id
    @Column(name = "org_id")
    private UUID orgId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String role; // e.g. ADMIN, MEMBER

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationUserId implements Serializable {
        private UUID orgId;
        private Long userId;
    }
}
