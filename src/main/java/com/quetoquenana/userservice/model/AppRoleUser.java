package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "app_roles_users", uniqueConstraints = {
        @UniqueConstraint(name = "ux_aru_user_app", columnNames = {"user_id", "app_role_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AppRoleUser extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonView(Application.ApplicationDetail.class)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonView({User.UserList.class, Application.ApplicationDetail.class})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_role_id", nullable = false)
    @JsonView({Application.ApplicationDetail.class, User.UserDetail.class})
    private AppRole role;

    public static AppRoleUser of(User user, AppRole role) {
        return AppRoleUser.builder()
                .user(user)
                .role(role)
                .build();
    }
}