package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.ApplicationCreateRequest;
import com.quetoquenana.userservice.dto.ApplicationUpdateRequest;
import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

    @Column(name = "name", nullable = false, unique = true, length = 255)
    @JsonView(ApplicationList.class)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    @JsonView(ApplicationDetail.class)
    private String description;

    @Column(name = "is_active", nullable = false)
    @JsonView(ApplicationList.class)
    private boolean isActive = true;

    public static Application fromCreateRequest(ApplicationCreateRequest request) {
        return Application.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public void updateFromRequest(ApplicationUpdateRequest request, String username) {
        if (request.getDescription() != null) this.setDescription(request.getDescription());
        if (request.getIsActive() != null) this.setActive(request.getIsActive());

        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(username);
    }

    // JSON Views
    public static class ApplicationList extends ApiBaseResponseView.Always {}
    public static class ApplicationDetail extends Application.ApplicationList {}
}

