package com.quetoquenana.personservice.service;

import com.quetoquenana.personservice.dto.PersonCreateRequest;
import com.quetoquenana.personservice.dto.PersonUpdateRequest;
import com.quetoquenana.personservice.model.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonService {
    List<Person> findAll();

    Page<Person> findAll(Pageable pageable);

    Optional<Person> findById(UUID id);

    Person save(PersonCreateRequest request);

    Person update(UUID id, PersonUpdateRequest request);

    void deleteById(UUID id);

    Optional<Person> findByIdNumber(String idNumber);
}
