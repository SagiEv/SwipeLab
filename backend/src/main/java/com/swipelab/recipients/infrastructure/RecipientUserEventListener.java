package com.swipelab.recipients.infrastructure;

import com.swipelab.recipients.domain.RecipientUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = "user-events", groupId = "recipient-group")
public class RecipientUserEventListener {

    private final RecipientUserRepository recipientUserRepository;

    @KafkaHandler
    public void handleUserCreated(com.swipelab.users.events.UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for user: {}", event.getUsername());

        if (!recipientUserRepository.existsById(event.getUsername())) {
            RecipientUser recipientUser = RecipientUser.builder()
                    .username(event.getUsername())
                    .active(true)
                    .build();
            recipientUserRepository.save(recipientUser);
            log.info("Created RecipientUser for: {}", event.getUsername());
        } else {
            log.info("RecipientUser already exists: {}", event.getUsername());
        }
    }

    @KafkaHandler
    public void handleUserStatusChanged(com.swipelab.users.events.UserStatusChangedEvent event) {
        log.info("Received UserStatusChangedEvent for user: {}", event.getUsername());
        recipientUserRepository.findById(event.getUsername()).ifPresent(user -> {
            user.setActive(event.isActive());
            recipientUserRepository.save(user);
            log.info("Updated status for user: {} to active={}", event.getUsername(), event.isActive());
        });
    }

    // Default handler to catch any other event types thrown onto the "user-events" topic
    // without crashing the listener or throwing MessageConversionException
    @KafkaHandler(isDefault = true)
    public void handleUnknown(Object object) {
        log.warn("Received unknown or unmapped event on user-events topic: {}", object.getClass().getName());
    }
}

