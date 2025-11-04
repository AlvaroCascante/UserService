package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonView(UserList.class)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    @JsonView(UserList.class)
    private String username;

    @Column(name = "password_hash", nullable = false)
    @JsonView(ApiBaseResponseView.NoShow.class)
    private String passwordHash;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    @JsonView(UserDetail.class)
    private Person person;

    @Column(name = "nickname", length = 50)
    @JsonView(UserList.class)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    @JsonView(UserList.class)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private UserStatus userStatus = UserStatus.ACTIVE;

    // JSON Views
    public static class UserList extends ApiBaseResponseView.Always {}
    public static class UserDetail extends User.UserList {}

    public static User fromCreateRequest(
            UserCreateRequest request,
            String passwordHash,
            UserStatus status,
            Person person
    ) {
        return User.builder()
                .username(request.getUsername())
                .passwordHash(passwordHash)
                .person(person)
                .nickname(request.getNickname())
                .userStatus(status)
                .build();
    }

    public void updateFromRequest(UserUpdateRequest request, String username) {
        if (request.getNickname() != null) this.setNickname(request.getNickname());
        if (request.getUserStatus() != null) this.setUserStatus(UserStatus.valueOf(request.getUserStatus()));

        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(username);
    }

    public void updateStatus(UserStatus newStatus, String username) {
        this.updateStatus(newStatus, null, username);
    }

    public void updateStatus(UserStatus newStatus, String passwordHash, String username) {
        this.setUserStatus(newStatus);
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(username);

        switch (newStatus) {
            case ACTIVE:
            case RESET:
                this.setPasswordHash(passwordHash);
                break;

            case INACTIVE:
            case BLOCKED:
                break;
            default:
                throw new IllegalArgumentException("Invalid user status: " + newStatus);
        }
    }
}
