package com.quetoquenana.userservice.util;

import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.model.*;
import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestEntityFactory {

    public static final String DEFAULT_ID_NUMBER = "ID123456";
    public static final String DEFAULT_USERNAME = "user@email.com";
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

    public static User createUser(
            LocalDateTime createdAt,
            String createdBy
    ) {
        User user = User.builder()
                .username(DEFAULT_USERNAME)
                .passwordHash("passwordHash")
                .nickname("nick")
                .userStatus(UserStatus.ACTIVE)
                .build();
        user.setCreatedAt(createdAt);
        user.setCreatedBy(createdBy);
        return user;
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
        return req;
    }

    public static UserCreateRequest getUserCreateRequest() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername(DEFAULT_USERNAME);
        req.setNickname("nick");
        req.setPerson(getPersonCreateRequest(DEFAULT_ID_NUMBER, true));
        return req;
    }

    public static PersonUpdateRequest getPersonUpdateRequest(Boolean isActive) {
        PersonUpdateRequest req = new PersonUpdateRequest();
        req.setName("John");
        req.setLastname("White");
        req.setIsActive(isActive);
        return req;
    }

    public static UserUpdateRequest getUserUpdateRequest() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setNickname("nickname");
        req.setUserStatus(UserStatus.ACTIVE.name());
        return req;
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

}
