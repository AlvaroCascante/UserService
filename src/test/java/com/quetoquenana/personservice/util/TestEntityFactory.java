package com.quetoquenana.personservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.personservice.model.*;
import com.quetoquenana.personservice.dto.PersonCreateRequest;
import com.quetoquenana.personservice.dto.PersonUpdateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestEntityFactory {

    public static final String DEFAULT_ID_NUMBER = "ID123456";
    public static final String DEFAULT_USER = "testUser";
    public static final String ROLE_ADMIN = "ADMIN";

    public static Person createPerson() {
        return createPerson(LocalDateTime.now(), DEFAULT_USER);
    }

    public static Person createPerson(
            Boolean isActive
    ) {
        return createPerson(LocalDateTime.now(), DEFAULT_USER, DEFAULT_ID_NUMBER, isActive);
    }

    public static Person createPerson(
            String idNumber,
            Boolean isActive
    ) {
        return createPerson(LocalDateTime.now(), DEFAULT_USER, idNumber, isActive);
    }

    public static Person createPerson(
            LocalDateTime createdAt,
            String createdBy
    ) {
        return createPerson(createdAt, createdBy, DEFAULT_ID_NUMBER, true);
    }

    public static Person createPerson(
            LocalDateTime createdAt,
            String createdBy,
            String idNumber,
            Boolean isActive
    ) {
        Person person = Person.builder()
                .name("John")
                .lastname("White")
                .idNumber(idNumber)
                .isActive(isActive)
                .build();
        person.setCreatedAt(createdAt);
        person.setCreatedBy(createdBy);
        return person;
    }

    public static PersonCreateRequest getPersonCreateRequest(String idNumber, Boolean isActive) {
        PersonCreateRequest req = new PersonCreateRequest();
        req.setIdNumber(idNumber);
        req.setName("John");
        req.setLastname("White");
        req.setIsActive(isActive);
        return req;
    }

    public static String createPersonPayload(ObjectMapper objectMapper) {
        try {
            PersonCreateRequest req = getPersonCreateRequest(DEFAULT_ID_NUMBER, true);
            return objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String createPersonPayload(ObjectMapper objectMapper, boolean isActive) {
        try {
            PersonCreateRequest req = getPersonCreateRequest(DEFAULT_ID_NUMBER, isActive);
            return objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PersonUpdateRequest getPersonUpdateRequest(Boolean isActive) {
        PersonUpdateRequest req = new PersonUpdateRequest();
        req.setName("John");
        req.setLastname("White");
        req.setIsActive(isActive);
        return req;
    }

    public static String createPersonUpdatePayload(ObjectMapper objectMapper, Boolean isActive) {
        try {
            PersonUpdateRequest req = getPersonUpdateRequest(isActive);
            return objectMapper.writeValueAsString(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Profile createProfile(Person person) {
        Profile profile = Profile.builder()
                .person(person)
                .birthday(LocalDate.of(1990, 1, 1))
                .gender("M")
                .nationality("TestNationality")
                .maritalStatus("Single")
                .occupation("Engineer")
                .profilePictureUrl("https://example.com/profile.jpg")
                .build();
        profile.setCreatedAt(LocalDateTime.now());
        profile.setCreatedBy(DEFAULT_USER);
        return profile;
    }

    public static String createProfilePayload(ObjectMapper objectMapper) {
        Profile profilePayload = Profile.builder()
                .birthday(LocalDate.of(1990, 1, 1))
                .gender("M")
                .nationality("TestNationality")
                .maritalStatus("Single")
                .occupation("Engineer")
                .profilePictureUrl("https://example.com/profile.jpg")
                .build();
        try {
            return objectMapper.writeValueAsString(profilePayload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Phone createPhone(Person person, String phoneNumber) {
        return Phone.builder()
                .person(person)
                .category(PhoneCategory.HOME)
                .isMain(true)
                .phoneNumber(phoneNumber)
                .build();
    }
    public static Address createAddress(Person person, String country) {
        return Address.builder()
                .person(person)
                .addressType(AddressType.HOME)
                .state("State")
                .city("City")
                .country(country)
                .zipCode("12345")
                .address("123 Main St")
                .build();
    }

    public static Address createAddress(Person person) {
        return createAddress(person, "Country");
    }

    public static String createPhonePayload(ObjectMapper objectMapper, String phoneNumber) {
        try {
            return objectMapper.writeValueAsString(createPhone(null, phoneNumber));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize phone payload", e);
        }
    }

    public static String createAddressPayload(ObjectMapper objectMapper, String country) {
        try {
            return objectMapper.writeValueAsString(createAddress(null, country));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize phone payload", e);
        }
    }

    public static String createAddressPayload(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(createAddress(null));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize phone payload", e);
        }
    }
}
