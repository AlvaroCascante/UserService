package com.quetoquenana.personservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.dto.AddressCreateRequest;
import com.quetoquenana.personservice.dto.AddressUpdateRequest;
import com.quetoquenana.personservice.dto.PhoneUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
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

    @Column(name = "address", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private String address;

    @Column(name = "country", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private String country;

    @Column(name = "city", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private String city;

    @Column(name = "state", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private String state;

    @Column(name = "zip_code", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private String zipCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    @JsonView(Person.PersonDetail.class)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private AddressType addressType;

    public static Address fromCreateRequest(AddressCreateRequest request) {
        return Address.builder()
                .address(request.getAddress())
                .country(request.getCountry())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .addressType(request.getAddressType())
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
