package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "applications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Application extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonView(ApplicationList.class)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    @JsonView(ApplicationList.class)
    private String name;

    @Column(name = "description", length = 100)
    @JsonView(ApplicationDetail.class)
    private String description;

    @Column(name = "is_active", nullable = false)
    @JsonView(ApplicationList.class)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(value = ApplicationDetail.class)
    private Set<AppRole> roles = new HashSet<>();

    public void addRole(AppRole role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        role.setApplication(this);
        roles.add(role);
    }

    public static Application fromCreateRequest(ApplicationCreateRequest request) {
        return Application.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(Optional.ofNullable(request.getIsActive()).orElse(true))
                .build();
    }

    public void updateFromRequest(ApplicationUpdateRequest request, String username) {
        if (request.getDescription() != null) this.setDescription(request.getDescription());
        if (request.getIsActive() != null) this.setIsActive(request.getIsActive());

        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(username);
    }

    // JSON Views
    public static class ApplicationList extends ApiBaseResponseView.Always {}
    public static class ApplicationDetail extends Application.ApplicationList {}
}
