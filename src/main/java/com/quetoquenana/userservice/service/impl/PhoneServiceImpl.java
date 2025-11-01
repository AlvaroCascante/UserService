package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.PhoneCreateRequest;
import com.quetoquenana.userservice.dto.PhoneUpdateRequest;
import com.quetoquenana.userservice.exception.InactiveRecordException;
import com.quetoquenana.userservice.exception.RecordNotDeletableException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.Phone;
import com.quetoquenana.userservice.repository.PersonRepository;
import com.quetoquenana.userservice.repository.PhoneRepository;
import com.quetoquenana.userservice.service.CurrentUserService;
import com.quetoquenana.userservice.service.PhoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhoneServiceImpl implements PhoneService {

    private final PhoneRepository phoneRepository;
    private final PersonRepository personRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    @Override
    public Phone addPhoneToPerson(UUID personId, PhoneCreateRequest request) {
        Person person = personRepository.findById(personId)
                .orElseThrow(RecordNotFoundException::new);
        if (!person.isActive()) {
            throw new InactiveRecordException("person.inactive");
        }
        Phone phone = Phone.fromCreateRequest(request);
        phone.setPerson(person);

        person.addPhone(phone);
        person.setUpdatedAt(LocalDateTime.now());
        person.setUpdatedBy(currentUserService.getCurrentUsername());
        phoneRepository.save(phone);
        if(phone.isMain()) {
            phoneRepository.clearMainForPerson(personId, phone.getId());
        }
        return phone;
    }

    @Transactional
    @Override
    public Phone updatePhone(UUID phoneId, PhoneUpdateRequest request) {
        Phone existingPhone = phoneRepository.findById(phoneId)
                .orElseThrow(RecordNotFoundException::new);
        Person person = existingPhone.getPerson();
        if (!person.isActive()) {
            throw new InactiveRecordException("person.inactive");
        }
        existingPhone.updateFromRequest(request);
        if(request.getIsMain() != null && request.getIsMain()) {
            phoneRepository.clearMainForPerson(person.getId(), phoneId);
        }
        return phoneRepository.save(existingPhone);
    }

    @Transactional
    @Override
    public void deleteById(UUID phoneId) {
        Phone existingPhone = phoneRepository.findById(phoneId)
                .orElseThrow(RecordNotFoundException::new);
        if (!existingPhone.getPerson().isActive()) {
            throw new InactiveRecordException();
        }
        if (existingPhone.isMain()) {
            throw new RecordNotDeletableException();
        }
        phoneRepository.delete(existingPhone);
    }
}