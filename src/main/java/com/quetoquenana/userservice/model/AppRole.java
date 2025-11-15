// ...existing code...
package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.AppRoleCreateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "app_roles", uniqueConstraints = {
        @UniqueConstraint(name = "ux_app_roles_app_name", columnNames = {"application_id", "role_name"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AppRole extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonView(Application.ApplicationList.class)
    private Application application;

    @Column(name = "role_name", nullable = false, length = 50)
    @JsonView(Application.ApplicationList.class)
    private String roleName;

    @Column(name = "description", length = 100)
    @JsonView(Application.ApplicationDetail.class)
    private String description;

    public static AppRole fromCreateRequest(Application application, AppRoleCreateRequest request) {
        return AppRole.builder()
                .id(UUID.randomUUID())
                .application(application)
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .build();
    }

    public void updateFromRequest(String description) {
        if (description != null) this.setDescription(description);
    }
}

