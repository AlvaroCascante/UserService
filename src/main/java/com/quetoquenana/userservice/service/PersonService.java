package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import com.quetoquenana.userservice.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonService {
    List<Person> findAll();

    List<Person> findByIsActive(boolean isActive);

    Page<Person> findAll(Pageable pageable);

    Optional<Person> findById(UUID id);

    Person save(PersonCreateRequest request);

    Person update(UUID id, PersonUpdateRequest request);

    void activateById(UUID id);

    void deleteById(UUID id);

    Optional<Person> findByIdNumber(String idNumber);
}
