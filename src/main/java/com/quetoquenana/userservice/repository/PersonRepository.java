package com.quetoquenana.userservice.repository;

import com.quetoquenana.userservice.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {
    //TODO manage status methods
    Optional<Person> findByIdNumber(String idNumber);
    List<Person> findByIsActive(boolean isActive);
    Optional<Person> findByIdAndIsActive(UUID id, boolean isActive);
}
