package com.quetoquenana.personservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.dto.ProfileCreateRequest;
import com.quetoquenana.personservice.dto.ProfileUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Profile extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonView(Person.PersonDetail.class)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_person", unique = true)
    private Person person;

    @Column(name = "birthday")
    @JsonView(Person.PersonDetail.class)
    private LocalDate birthday;

    @Column(name = "gender")
    @JsonView(Person.PersonDetail.class)
    private String gender;

    @Column(name = "nationality")
    @JsonView(Person.PersonDetail.class)
    private String nationality;

    @Column(name = "marital_status")
    @JsonView(Person.PersonDetail.class)
    private String maritalStatus;

    @Column(name = "occupation")
    @JsonView(Person.PersonDetail.class)
    private String occupation;

    @Column(name = "profile_picture_url")
    @JsonView(Person.PersonDetail.class)
    private String profilePictureUrl;

    public static Profile fromAddRequest(ProfileCreateRequest request) {
        return Profile.builder()
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .maritalStatus(request.getMaritalStatus())
                .occupation(request.getOccupation())
                .profilePictureUrl(request.getProfilePictureUrl())
                .build();
    }

    public void updateFromRequest(ProfileUpdateRequest request, String username) {
        if(request.getBirthday() != null) {
            this.birthday = request.getBirthday();
        }
        if(request.getGender() != null) {
            this.gender = request.getGender();
        }
        if(request.getNationality() != null) {
            this.nationality = request.getNationality();
        }
        if(request.getMaritalStatus() != null) {
            this.maritalStatus = request.getMaritalStatus();
        }
        if(request.getOccupation() != null) {
            this.occupation = request.getOccupation();
        }
        if(request.getProfilePictureUrl() != null) {
            this.profilePictureUrl = request.getProfilePictureUrl();
        }
        this.setUpdatedAt(java.time.LocalDateTime.now());
        this.setUpdatedBy(username);
    }
}
