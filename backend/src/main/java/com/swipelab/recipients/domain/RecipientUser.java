package com.swipelab.recipients.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recipient_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RecipientUser {

    @Id
    @lombok.EqualsAndHashCode.Include
    private String username;

    @Builder.Default
    private boolean active = true;

}
