// ...existing code...
package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "app_roles_users", uniqueConstraints = {
        @UniqueConstraint(name = "ux_uar_user_app", columnNames = {"user_id", "application_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AppRoleUser extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonView(User.UserList.class)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonView(AppRole.class)
    private AppRole role;

    public static AppRoleUser of(User user, Application application, AppRole role) {
        return AppRoleUser.builder()
                .id(UUID.randomUUID())
                .user(user)
                .role(role)
                .build();
    }
}