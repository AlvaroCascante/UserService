package com.quetoquenana.personservice.controller;

import com.quetoquenana.personservice.dto.PhoneCreateRequest;
import com.quetoquenana.personservice.dto.PhoneUpdateRequest;
import com.quetoquenana.personservice.exception.InactiveRecordException;
import com.quetoquenana.personservice.exception.RecordNotFoundException;
import com.quetoquenana.personservice.model.Phone;
import com.quetoquenana.personservice.model.PhoneCategory;
import com.quetoquenana.personservice.service.PhoneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PhoneControllerTest {
    @Mock
    private PhoneService phoneService;

    @InjectMocks
    private PhoneController phoneController;

    private Phone phone;
    private UUID personId;
    private UUID phoneId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personId = UUID.randomUUID();
        phoneId = UUID.randomUUID();
        phone = Phone.builder()
                .id(phoneId)
                .category(PhoneCategory.HOME)
                .phoneNumber("1234567890")
                .isMain(true)
                .build();
    }

    @Test
    void addPhone_shouldReturnPhone() {
        when(phoneService.addPhoneToPerson(eq(personId), any(PhoneCreateRequest.class))).thenReturn(phone);
        ResponseEntity<Phone> response = phoneController.addPhone(personId, new PhoneCreateRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(phone, response.getBody());
    }

    @Test
    void updatePhone_shouldReturnPhone() {
        when(phoneService.updatePhone(eq(phoneId), any(PhoneUpdateRequest.class))).thenReturn(phone);
        ResponseEntity<Phone> response = phoneController.updatePhone(phoneId, new PhoneUpdateRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(phone, response.getBody());
    }

    @Test
    void deletePhone_shouldReturnNoContent() {
        doNothing().when(phoneService).deleteById(phoneId);
        ResponseEntity<?> response = phoneController.deletePhone(phoneId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addPhone_shouldThrowRecordNotFoundException() {
        when(phoneService.addPhoneToPerson(eq(personId), any(PhoneCreateRequest.class)))
                .thenThrow(new RecordNotFoundException("Person not found"));
        assertThrows(RecordNotFoundException.class, () -> phoneController
                .addPhone(personId, new PhoneCreateRequest()));
    }

    @Test
    void addPhone_shouldThrowInactiveRecordException() {
        when(phoneService.addPhoneToPerson(eq(personId), any(PhoneCreateRequest.class)))
                .thenThrow(new InactiveRecordException("Person is inactive"));
        assertThrows(InactiveRecordException.class, () -> phoneController
                .addPhone(personId, new PhoneCreateRequest()));
    }

    @Test
    void updatePhone_shouldThrowRecordNotFoundException() {
        when(phoneService.updatePhone(eq(phoneId), any(PhoneUpdateRequest.class)))
                .thenThrow(new RecordNotFoundException("Person not found"));
        assertThrows(RecordNotFoundException.class, () -> phoneController
                .updatePhone(phoneId, new PhoneUpdateRequest()));
    }

    @Test
    void updatePhone_shouldThrowInactiveRecordException() {
        when(phoneService.updatePhone(eq(phoneId), any(PhoneUpdateRequest.class)))
                .thenThrow(new InactiveRecordException("Person is inactive"));
        assertThrows(InactiveRecordException.class, () -> phoneController
                .updatePhone(phoneId, new PhoneUpdateRequest()));
    }

    @Test
    void deletePhone_shouldThrowRecordNotFoundException() {
        doThrow(new RecordNotFoundException("Phone not found")).when(phoneService).deleteById(phoneId);
        assertThrows(RecordNotFoundException.class, () -> phoneController
                .deletePhone(phoneId));
    }

    @Test
    void deletePhone_shouldThrowInactiveRecordException() {
        doThrow(new InactiveRecordException("Phone is inactive")).when(phoneService).deleteById(phoneId);
        assertThrows(InactiveRecordException.class, () -> phoneController
                .deletePhone(phoneId));
    }
}
