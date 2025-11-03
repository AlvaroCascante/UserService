package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.repository.PersonRepository;
import com.quetoquenana.userservice.service.CurrentUserService;
import com.quetoquenana.userservice.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    @Override
    public List<Person> findByIsActive(boolean isActive) {
        return personRepository.findByIsActive(isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Person> findAll(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Person> findById(UUID id) {
        return personRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Person> findByIdNumber(String idNumber) { return personRepository.findByIdNumber(idNumber);}

    @Override
    @Transactional
    public Person save(PersonCreateRequest request) {
        String username = currentUserService.getCurrentUsername();
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
    @Transactional
    public Person update(UUID id, PersonUpdateRequest request) {
        Person existing = personRepository.findById(id)
            .orElseThrow(RecordNotFoundException::new);
        existing.updateFromRequest(request, currentUserService.getCurrentUsername());
        return personRepository.save(existing);
    }

    @Override
    public void activateById(UUID id) {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        existingPerson.activate(currentUserService.getCurrentUsername());
        personRepository.save(existingPerson);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        Person existingPerson = personRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        if (existingPerson.isActive()) {
            existingPerson.setActive(false);
            existingPerson.setUpdatedAt(LocalDateTime.now());
            existingPerson.setUpdatedBy(currentUserService.getCurrentUsername());
            personRepository.save(existingPerson);
        }
    }
}
