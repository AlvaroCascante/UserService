package com.quetoquenana.personservice.model;


import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.dto.PhoneCreateRequest;
import com.quetoquenana.personservice.dto.PhoneUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Table(name = "phones")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Phone {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonView(Person.PersonDetail.class)
    private UUID id;

    @Column(name = "phone_number", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @JsonView(Person.PersonDetail.class)
    @JdbcType(value = PostgreSQLEnumJdbcType.class)
    private PhoneCategory category;

    @Column(name = "is_main", nullable = false)
    @JsonView(Person.PersonDetail.class)
    private boolean isMain = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    public static Phone fromCreateRequest(PhoneCreateRequest request) {
        return Phone.builder()
                .isMain(request.getIsMain())
                .phoneNumber(request.getPhoneNumber())
                .category(request.getCategory())
                .build();
    }

    public void updateFromRequest(PhoneUpdateRequest request) {
        if (request.getPhoneNumber() != null) {
            this.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getCategory() != null) {
            this.setCategory(PhoneCategory.valueOf(request.getCategory()));
        }
        if (request.getIsMain() != null) {
            this.setMain(request.getIsMain());
        }
    }
}