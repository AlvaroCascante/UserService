package com.quetoquenana.userservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.AddressCreateRequest;
import com.quetoquenana.userservice.dto.AddressUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonView(Person.PersonDetail.class)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "address", nullable = false, length = 100)
    @JsonView(Person.PersonDetail.class)
    private String address;

    @Column(name = "country", nullable = false, length = 100)
    @JsonView(Person.PersonDetail.class)
    private String country;

    @Column(name = "city", nullable = false, length = 100)
    @JsonView(Person.PersonDetail.class)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    @JsonView(Person.PersonDetail.class)
    private String state;

    @Column(name = "zip_code", nullable = false, length = 20)
    @JsonView(Person.PersonDetail.class)
    private String zipCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private AddressType addressType;

    public static Address fromCreateRequest(AddressCreateRequest request) {
        return Address.builder()
                .address(request.getAddress())
                .country(request.getCountry())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .addressType(AddressType.valueOf(request.getAddressType()))
                .build();
    }

    public void updateFromRequest(AddressUpdateRequest request) {
        if (request.getCountry() != null) {
            this.setCountry(request.getCountry());
        }
        if (request.getAddressType() != null) {
            this.setAddressType(AddressType.valueOf(request.getAddressType()));
        }
        if (request.getCity() != null) {
            this.setCity(request.getCity());
        }
        if (request.getState() != null) {
            this.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            this.setZipCode(request.getZipCode());
        }
        if (request.getAddress() != null) {
            this.setAddress(request.getAddress());
        }
    }
}
