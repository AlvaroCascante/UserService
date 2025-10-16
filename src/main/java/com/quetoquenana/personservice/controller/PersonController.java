package com.quetoquenana.personservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.dto.PersonCreateRequest;
import com.quetoquenana.personservice.dto.PersonUpdateRequest;
import com.quetoquenana.personservice.exception.RecordNotFoundException;
import com.quetoquenana.personservice.model.ApiResponse;
import com.quetoquenana.personservice.model.Person;
import com.quetoquenana.personservice.service.PersonService;
import com.quetoquenana.personservice.util.JsonViewPageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Slf4j
public class PersonController {
    private final PersonService personService;

    @GetMapping
    @JsonView(Person.PersonList.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')") // Only ADMIN and AUDITOR roles can access
    public ResponseEntity<ApiResponse> getAllPersons() {
        log.info("GET /api/persons called");
        List<Person> entities = personService.findAll();
        return ResponseEntity.ok(new ApiResponse(entities));
    }

    @GetMapping("/page")
    @JsonView(Person.PersonList.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')") // Only ADMIN and AUDITOR roles can access
    public ResponseEntity<ApiResponse> getPersonsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("GET /api/persons/page called with page={}, size={}", page, size);
        Page<Person> entities = personService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @GetMapping("/{id}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('USER')") // ADMIN, AUDITOR and USER roles can access
    public ResponseEntity<ApiResponse> getPersonById(
            @PathVariable UUID id
    ) {
        log.info("GET /api/persons/{} called", id);
        return personService.findById(id)
                .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
                .orElseGet(() -> {
                    log.error("Person with id {} not found", id);
                    throw new RecordNotFoundException();
                });
    }

    @GetMapping("/idNumber/{idNumber}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('USER')") // ADMIN, AUDITOR and USER roles can access
    public ResponseEntity<ApiResponse> getPersonByIdNumber(
            @PathVariable String idNumber
    ) {
        log.info("GET /api/persons/idNumber/{} called", idNumber);
        return personService.findByIdNumber(idNumber)
                .map(entity -> ResponseEntity.ok(new ApiResponse(entity)))
                .orElseGet(() -> {
                    log.error("Person with idNumber {} not found", idNumber);
                    throw new RecordNotFoundException();
                });
    }

    @PostMapping
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    public ResponseEntity<ApiResponse> createPerson(
            @RequestBody PersonCreateRequest request
    ) {
        log.info("POST /api/persons called with payload: {}", request);
        Person entity = personService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(entity));
    }

    @PutMapping("/{id}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    public ResponseEntity<ApiResponse> updatePerson(
            @PathVariable UUID id,
            @RequestBody PersonUpdateRequest request
    ) {
        log.info("PUT /api/persons/{} called with payload: {}", id, request);
        Person entity = personService.update(id, request);
        return ResponseEntity.ok(new ApiResponse(entity));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    public ResponseEntity<Void> deletePerson(
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/persons/{} called", id);
        personService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
