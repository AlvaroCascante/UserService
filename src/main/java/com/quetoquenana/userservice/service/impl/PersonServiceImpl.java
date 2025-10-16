package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.repository.PersonRepository;
import com.quetoquenana.userservice.service.PersonService;
import com.quetoquenana.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final UserService userService;

    @Override
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    @Override
    public Page<Person> findAll(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    @Override
    public Optional<Person> findById(UUID id) {
        return personRepository.findById(id);
    }

    @Override
    public Optional<Person> findByIdNumber(String idNumber) { return personRepository.findByIdNumber(idNumber);}

    @Override
    public Person save(PersonCreateRequest request) {
        String username = userService.getCurrentUsername();
        return personRepository.findByIdNumber(request.getIdNumber())
            .map(found -> {
                if (found.isActive()) {
                    throw new DuplicateRecordException("person.id.number.duplicate.active");
                } else {
                    found.setActive(true);
                    found.setUpdatedAt(LocalDateTime.now());
                    found.setUpdatedBy(username);
                    return personRepository.save(found);
                }
            })
            .orElseGet(() -> {
                Person person = Person.fromCreateRequest(request);
                person.setCreatedAt(LocalDateTime.now());
                person.setCreatedBy(username);
                return personRepository.save(person);
            });
    }

    @Override
    public Person update(UUID id, PersonUpdateRequest request) {
        Person existingPerson = personRepository.findById(id)
            .orElseThrow(RecordNotFoundException::new);
        existingPerson.updateFromRequest(request, userService.getCurrentUsername());
        return personRepository.save(existingPerson);
    }

    @Override
    public void deleteById(UUID id) {
        String username = userService.getCurrentUsername();
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        if (existingPerson.isActive()) {
            existingPerson.setActive(false);
            existingPerson.setUpdatedAt(LocalDateTime.now());
            existingPerson.setUpdatedBy(username);
            personRepository.save(existingPerson);
        }
    }
}
