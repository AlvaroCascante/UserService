package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "persons")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Person extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonView(PersonList.class)
    private UUID id;

    @Column(name = "id_number", nullable = false, unique = true, length = 50)
    @JsonView({PersonList.class,
            User.UserDetail.class})
    private String idNumber;

    @Column(name = "name", nullable = false, length = 50)
    @JsonView({PersonList.class, User.UserDetail.class, Application.ApplicationDetail.class})
    private String name;

    @Column(name = "lastname", nullable = false, length = 50)
    @JsonView({PersonList.class, User.UserDetail.class, Application.ApplicationDetail.class})
    private String lastname;

    @Column(name = "is_active", nullable = false)
    @JsonView(PersonList.class)
    private Boolean isActive;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonView(PersonDetail.class)
    private Set<Profile> profile;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(PersonDetail.class)
    private Set<Phone> phones = new HashSet<>();

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonView(PersonDetail.class)
    private Set<Address> addresses = new HashSet<>();

    public Profile getProfile() {
        if(profile == null || profile.isEmpty()) {
            return null;
        }
        return profile.iterator().next();
    }

    public void setProfile(Profile profile) {
        if(this.profile == null) {
            this.profile = new HashSet<>();
        }
        this.profile.add(profile);
        profile.setPerson(this);
    }

    // JSON Views to control serialization responses
    public static class PersonList extends ApiBaseResponseView.Always {}
    public static class PersonDetail extends Person.PersonList {}

    public void addPhone(Phone phone) {
        if (phones == null) {
            phones = new HashSet<>();
        }
        phones.add(phone);
        phone.setPerson(this);
    }

    public void addAddress(Address address) {
        if (addresses == null) {
            addresses = new HashSet<>();
        }
        addresses.add(address);
        address.setPerson(this);
    }

    public static Person fromCreateRequest(PersonCreateRequest request) {
        return Person.builder()
            .idNumber(request.getIdNumber())
            .name(request.getName())
            .lastname(request.getLastname())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .build();
    }

    public void updateFromRequest(PersonUpdateRequest request, String username) {
        if (request.getName() != null) this.setName(request.getName());
        if (request.getLastname() != null) this.setLastname(request.getLastname());
        if (request.getIsActive() != null) this.setIsActive(request.getIsActive());

        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(username);
    }

    public void activate(String username) {
        this.setIsActive(true);
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(username);
    }
}
