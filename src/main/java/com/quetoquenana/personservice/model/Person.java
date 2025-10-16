package com.quetoquenana.personservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.dto.PersonCreateRequest;
import com.quetoquenana.personservice.dto.PersonUpdateRequest;
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

    @Column(name = "id_number", nullable = false, unique = true)
    @JsonView(PersonList.class)
    private String idNumber;

    @Column(name = "name", nullable = false)
    @JsonView(PersonList.class)
    private String name;

    @Column(name = "lastname", nullable = false)
    @JsonView(PersonList.class)
    private String lastname;

    @Column(name = "is_active", nullable = false)
    @JsonView(PersonList.class)
    private boolean isActive;

    @OneToOne(mappedBy = "person", fetch = FetchType.LAZY)
    @JsonView(PersonDetail.class)
    private Profile profile;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonView(PersonDetail.class)
    private Set<Phone> phones = new HashSet<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonView(PersonDetail.class)
    private Set<Address> addresses = new HashSet<>();

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

    /**
     * Updates this Person with the non-null fields from the given PersonUpdateRequest.
     */
    public void updateFromRequest(PersonUpdateRequest request, String username) {
        if (request.getName() != null) this.setName(request.getName());
        if (request.getLastname() != null) this.setLastname(request.getLastname());
        if (request.getIsActive() != null) this.setActive(request.getIsActive());

        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(username);
    }
}
