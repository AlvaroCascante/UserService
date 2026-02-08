package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.dto.ApiResponse;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.service.PersonService;
import com.quetoquenana.userservice.util.JsonViewPageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.quetoquenana.userservice.util.Constants.Pagination.PAGE;
import static com.quetoquenana.userservice.util.Constants.Pagination.PAGE_SIZE;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Slf4j
public class PersonController {

    private final PersonService personService;

    @GetMapping("/status/{status}")
    @JsonView(Person.PersonList.class)
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    public ResponseEntity<ApiResponse> getPersonsByStatus(
            @PathVariable Boolean status
    ) {
        log.info("GET /api/persons/status{} called", status);
        List<Person> entities = personService.findByIsActive(status);
        return ResponseEntity.ok(new ApiResponse(entities));
    }

    @GetMapping("/page")
    @JsonView(Person.PersonList.class)
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN role can access
    public ResponseEntity<ApiResponse> getPersonsPage(
            @RequestParam(defaultValue = PAGE) int page,
            @RequestParam(defaultValue = PAGE_SIZE) int size
    ) {
        log.info("GET /api/persons/page called with page={}, size={}", page, size);
        Page<Person> entities = personService.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(new ApiResponse(new JsonViewPageUtil<>(entities, entities.getPageable())));
    }

    @GetMapping("/{id}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessIdPerson(authentication, #id)")
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
    @PreAuthorize("@securityService.canAccessIdNumber(authentication, #idNumber)")
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

    @PutMapping("/{id}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessIdPerson(authentication, #id)")
    public ResponseEntity<ApiResponse> updatePerson(
            @PathVariable UUID id,
            @RequestBody PersonUpdateRequest request
    ) {
        log.info("PUT /api/persons/{} called with payload: {}", id, request);
        Person entity = personService.update(id, request);
        return ResponseEntity.ok(new ApiResponse(entity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.canAccessIdPerson(authentication, #id)")
    public ResponseEntity<Void> deletePerson(
            @PathVariable UUID id
    ) {
        log.info("DELETE /api/persons/{} called", id);
        personService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
