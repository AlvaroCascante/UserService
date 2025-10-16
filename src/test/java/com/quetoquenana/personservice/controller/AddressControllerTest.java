package com.quetoquenana.personservice.controller;

import com.quetoquenana.personservice.dto.AddressCreateRequest;
import com.quetoquenana.personservice.dto.AddressUpdateRequest;
import com.quetoquenana.personservice.dto.PhoneCreateRequest;
import com.quetoquenana.personservice.dto.PhoneUpdateRequest;
import com.quetoquenana.personservice.exception.InactiveRecordException;
import com.quetoquenana.personservice.exception.RecordNotFoundException;
import com.quetoquenana.personservice.model.Address;
import com.quetoquenana.personservice.model.AddressType;
import com.quetoquenana.personservice.service.AddressService;
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

class AddressControllerTest {
    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    private Address address;
    private UUID personId;
    private UUID addressId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personId = UUID.randomUUID();
        addressId = UUID.randomUUID();
        address = Address.builder()
                .id(addressId)
                .country("Spain")
                .city("Madrid")
                .state("Community of Madrid")
                .zipCode("28001")
                .addressType(AddressType.HOME)
                .address("123 Main St")
                .build();
    }

    @Test
    void createAddress_shouldReturnAddress() {
        when(addressService.addAddressToPerson(eq(personId), any(AddressCreateRequest.class))).thenReturn(address);
        ResponseEntity<Address> response = addressController.createAddress(personId, new AddressCreateRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(address, response.getBody());
    }

    @Test
    void updateAddress_shouldReturnAddress() {
        when(addressService.updateAddress(eq(addressId), any(AddressUpdateRequest.class))).thenReturn(address);
        ResponseEntity<Address> response = addressController.updateAddress(addressId, new AddressUpdateRequest());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(address, response.getBody());
    }

    @Test
    void deleteAddress_shouldReturnNoContent() {
        doNothing().when(addressService).deleteById(addressId);
        ResponseEntity<Void> response = addressController.deleteAddress(addressId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void addPhone_shouldThrowRecordNotFoundException() {
        when(addressService.addAddressToPerson(eq(personId), any(AddressCreateRequest.class)))
                .thenThrow(new RecordNotFoundException("Person not found"));
        assertThrows(RecordNotFoundException.class, () ->
                addressController.createAddress(personId, new AddressCreateRequest()));
    }

    @Test
    void addPhone_shouldThrowInactiveRecordException() {
        when(addressService.addAddressToPerson(eq(personId), any(AddressCreateRequest.class)))
                .thenThrow(new InactiveRecordException("Person is inactive"));
        assertThrows(InactiveRecordException.class, () ->
                addressController.createAddress(personId, new AddressCreateRequest()));
    }

    @Test
    void updatePhone_shouldThrowRecordNotFoundException() {
        when(addressService.updateAddress(eq(addressId), any(AddressUpdateRequest.class)))
                .thenThrow(new RecordNotFoundException("Person not found"));
        assertThrows(RecordNotFoundException.class, () ->
                addressController.updateAddress(addressId, new AddressUpdateRequest()));
    }

    @Test
    void updatePhone_shouldThrowInactiveRecordException() {
        when(addressService.updateAddress(eq(addressId), any(AddressUpdateRequest.class)))
                .thenThrow(new InactiveRecordException("Person is inactive"));
        assertThrows(InactiveRecordException.class, () ->
                addressController.updateAddress(addressId, new AddressUpdateRequest()));
    }

    @Test
    void deletePhone_shouldThrowRecordNotFoundException() {
        doThrow(new RecordNotFoundException("Phone not found")).when(addressService).deleteById(addressId);
        assertThrows(RecordNotFoundException.class, () ->
                addressController.deleteAddress(addressId));
    }

    @Test
    void deletePhone_shouldThrowInactiveRecordException() {
        doThrow(new InactiveRecordException("Phone is inactive")).when(addressService).deleteById(addressId);
        assertThrows(InactiveRecordException.class, () ->
                addressController.deleteAddress(addressId));
    }
}

