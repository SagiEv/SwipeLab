package com.swipelab.collection.infrastructure;

import com.swipelab.collection.domain.UserCollectionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCollectionRepository extends JpaRepository<UserCollectionEntry, Long> {

    /** All YES-tagged images for a user, newest first. */
    List<UserCollectionEntry> findByUsernameOrderByTaggedAtDesc(String username);

    long countByUsername(String username);
}
