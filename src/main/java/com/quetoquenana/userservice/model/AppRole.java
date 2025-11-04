// ...existing code...
package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
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
    private String rolName;

    @Column(name = "description", length = 100)
    @JsonView(Application.ApplicationDetail.class)
    private String description;

    public static AppRole fromCreateRequest(Application application, String name, String description) {
        return AppRole.builder()
                .id(UUID.randomUUID())
                .application(application)
                .rolName(name)
                .description(description)
                .build();
    }

    public void updateFromRequest(String description) {
        if (description != null) this.setDescription(description);
    }
}

