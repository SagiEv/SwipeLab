package com.swipelab.repository;

import com.swipelab.model.entity.RecipientGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipientGroupRepository
        extends JpaRepository<RecipientGroup, Long> {

    Optional<RecipientGroup> findByName(String name);

    boolean existsByName(String name);
}
