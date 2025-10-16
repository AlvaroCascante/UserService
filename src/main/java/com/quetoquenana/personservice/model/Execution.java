package com.quetoquenana.personservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonView;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Execution {
    @Id
    @JsonView({Execution.ExecutionList.class})
    private UUID id;

    @Column(name = "executed_at", nullable = false)
    @JsonView({Execution.ExecutionList.class})
    private LocalDateTime executedAt;

    @Column(name = "server_name")
    @JsonView({Execution.ExecutionDetail.class})
    private String serverName;

    @Column(name = "ip_address")
    @JsonView({Execution.ExecutionDetail.class})
    private String ipAddress;

    @Column(name = "app_version")
    @JsonView({Execution.ExecutionList.class})
    private String appVersion;

    @Column(name = "environment")
    @JsonView({Execution.ExecutionList.class})
    private String environment;

    // JSON Views to control serialization responses
    public static class ExecutionList extends ApiBaseResponseView.Always {}
    public static class ExecutionDetail extends ExecutionList {}
}
