package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quetoquenana.userservice.dto.PersonUpdateRequest;
import com.quetoquenana.userservice.exception.ImmutableFieldModificationException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.ApiResponse;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.service.PersonService;
import com.quetoquenana.userservice.util.TestEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PersonControllerTest {
    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonController personController;

    private Person person;
    private UUID personId;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personId = UUID.randomUUID();
        person = Person.builder()
                .id(personId)
                .name("John")
                .lastname("Doe")
                .idNumber("ID123456")
                .isActive(true)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testGetPersonById_NotFound() {
        when(personService.findById(personId)).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> personController.getPersonById(personId));
    }

    @Test
    void testUpdatePerson_NotFound() {
        PersonUpdateRequest updateRequest = TestEntityFactory.getPersonUpdateRequest(true);
        when(personService.update(personId, updateRequest)).thenThrow(new RecordNotFoundException());
        assertThrows(RecordNotFoundException.class, () -> personController.updatePerson(personId, updateRequest));
    }

    @Test
    void testUpdatePerson_ImmutableFieldModification() {
        PersonUpdateRequest updateRequest = TestEntityFactory.getPersonUpdateRequest(true);
        when(personService.update(personId, updateRequest)).thenThrow(new ImmutableFieldModificationException("person.id.number.immutable"));
        assertThrows(ImmutableFieldModificationException.class, () -> personController.updatePerson(personId, updateRequest));
    }

    @Test
    void testGetPersonById_Found() throws Exception {
        when(personService.findById(personId)).thenReturn(Optional.of(person));
        ResponseEntity<ApiResponse> response = personController.getPersonById(personId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        Person data = (Person) apiResponse.getData();
        assertEquals(person, data);
        String json = objectMapper.writerWithView(Person.PersonDetail.class).writeValueAsString(data);
        assertTrue(json.contains("id"));
        assertTrue(json.contains("name"));
        assertTrue(json.contains("lastname"));
        assertTrue(json.contains("idNumber"));
        assertTrue(json.contains("isActive"));
        assertTrue(json.contains("phones"));
    }

    @Test
    void testGetPersonsPage_ReturnsPage() throws Exception {
        Page<Person> page = new PageImpl<>(Collections.singletonList(person), PageRequest.of(0, 10), 1);
        when(personService.findAll(any())).thenReturn(page);
        ResponseEntity<ApiResponse> response = personController.getPersonsPage(0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        Page<?> data = (Page<?>) apiResponse.getData();
        assertEquals(1, data.getTotalElements());
        String json = objectMapper.writerWithView(Person.PersonList.class).writeValueAsString(((Page<?>) apiResponse.getData()).getContent());
        assertTrue(json.contains("id"));
        assertTrue(json.contains("name"));
        assertTrue(json.contains("lastname"));
        assertTrue(json.contains("isActive"));
    }

    @Test
    void testUpdatePerson_ReturnsUpdated() {
        PersonUpdateRequest updateRequest = TestEntityFactory.getPersonUpdateRequest(true);
        when(personService.update(personId, updateRequest)).thenReturn(person);
        ResponseEntity<ApiResponse> response = personController.updatePerson(personId, updateRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        Person data = (Person) apiResponse.getData();
        assertEquals(person, data);
    }

    // java
    @Test
    void testGetPersonsByStatus_ReturnsActive() throws Exception {
        List<Person> persons = Collections.singletonList(person); // person is active in setUp()
        when(personService.findByIsActive(true)).thenReturn(persons);

        ResponseEntity<ApiResponse> response = personController.getPersonsByStatus(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        List<?> data = (List<?>) apiResponse.getData();
        assertEquals(1, data.size());

        String json = objectMapper.writerWithView(Person.PersonList.class).writeValueAsString(data);
        assertTrue(json.contains("idNumber"));
        assertTrue(json.contains("isActive"));
    }

    @Test
    void testGetPersonsByStatus_ReturnsInactive() throws Exception {
        Person inactivePerson = Person.builder()
                .id(UUID.randomUUID())
                .name("Jane")
                .lastname("Doe")
                .idNumber("ID999999")
                .isActive(false)
                .build();
        when(personService.findByIsActive(false)).thenReturn(Collections.singletonList(inactivePerson));

        ResponseEntity<ApiResponse> response = personController.getPersonsByStatus(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        List<?> data = (List<?>) apiResponse.getData();
        assertEquals(1, data.size());

        String json = objectMapper.writerWithView(Person.PersonList.class).writeValueAsString(data);
        assertTrue(json.contains("idNumber"));
        assertTrue(json.contains("isActive"));
    }
}
