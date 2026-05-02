package com.ndash.identity_framework.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
public class UserRole {

    @EmbeddedId
    private UserRoleId id = new UserRoleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    public void setUser(User user) {
        this.user = user;

        if (this.id == null) {
            this.id = new UserRoleId();
        }

        this.id.setUserId(user != null ? user.getId() : null);
    }

    public void setRole(Role role) {
        this.role = role;

        if (this.id == null) {
            this.id = new UserRoleId();
        }

        this.id.setRoleId(role != null ? role.getId() : null);
    }

    @PrePersist
    @PreUpdate
    private void syncCompositeKey() {
        if (this.id == null) {
            this.id = new UserRoleId();
        }

        if (this.user != null) {
            this.id.setUserId(this.user.getId());
        }

        if (this.role != null) {
            this.id.setRoleId(this.role.getId());
        }

        if (this.assignedAt == null) {
            this.assignedAt = LocalDateTime.now();
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole)) return false;
        UserRole other = (UserRole) o;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "id=" + id +
                ", assignedAt=" + assignedAt +
                '}';
    }
}