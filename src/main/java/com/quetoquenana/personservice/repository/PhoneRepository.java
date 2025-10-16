package com.quetoquenana.personservice.repository;

import com.quetoquenana.personservice.model.Phone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PhoneRepository extends JpaRepository<Phone, UUID> {
    List<Phone> findByPersonId(UUID personId);

    @Modifying
    @Query("UPDATE Phone p SET p.isMain = false WHERE p.person.id = :personId AND p.id != :id")
    void clearMainForPerson(
            @Param("personId") UUID personId,
            @Param("id") UUID id
    );
}
