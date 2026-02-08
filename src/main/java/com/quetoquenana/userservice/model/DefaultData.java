package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.ApiBaseResponseView;
import com.quetoquenana.userservice.dto.DefaultDataCreateRequest;
import com.quetoquenana.userservice.dto.DefaultDataUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "default_data")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DefaultData extends Auditable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @JsonView(DefaultDataList.class)
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    @JsonView(DefaultDataList.class)
    private String dataName;

    @Column(name = "description", length = 100)
    @JsonView(DefaultDataDetail.class)
    private String description;

    @Column(name = "is_active", nullable = false)
    @JsonView(DefaultDataDetail.class)
    private Boolean isActive;

    @Column(name = "data_category", nullable = false, length = 50)
    @JsonView(DefaultDataList.class)
    @Enumerated(EnumType.STRING)
    private DataCategory dataCategory;

    public static DefaultData fromCreate(DefaultDataCreateRequest request) {
        Boolean active = request.getIsActive();
        return DefaultData.builder()
                .id(UUID.randomUUID())
                .dataName(request.getName())
                .description(request.getDescription())
                .dataCategory(DataCategory.valueOf(request.getDataCategory()))
                .isActive(active != null ? active : true)
                .build();
    }

    public void updateFromRequest(DefaultDataUpdateRequest request) {
        if (request.getName() != null) this.setDataName(request.getName());
        if (request.getDescription() != null) this.setDescription(request.getDescription());
        if (request.getIsActive() != null) this.setIsActive(request.getIsActive());
        if (request.getDataCategory() != null) this.setDataCategory(DataCategory.valueOf(request.getDataCategory()));
    }

    // JSON Views
    public static class DefaultDataList extends ApiBaseResponseView.Always {}
    public static class DefaultDataDetail extends DefaultDataList {}
}
