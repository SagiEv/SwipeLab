package com.swipelab.recipients.infrastructure;

import com.swipelab.recipients.domain.RecipientUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecipientUserEventListener {

    private final RecipientUserRepository recipientUserRepository;

    @Async
    @EventListener
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

    @Async
    @EventListener
    public void handleUserStatusChanged(com.swipelab.users.events.UserStatusChangedEvent event) {
        log.info("Received UserStatusChangedEvent for user: {}", event.getUsername());
        recipientUserRepository.findById(event.getUsername()).ifPresent(user -> {
            user.setActive(event.isActive());
            recipientUserRepository.save(user);
            log.info("Updated status for user: {} to active={}", event.getUsername(), event.isActive());
        });
    }

}

