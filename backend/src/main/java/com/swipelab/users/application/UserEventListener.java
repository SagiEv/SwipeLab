package com.swipelab.users.application;

import com.swipelab.users.domain.User;
import com.swipelab.users.events.FraudDetectedEvent;
import com.swipelab.users.events.UserStatusChangedEvent;
import com.swipelab.users.infrastructure.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventListener {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    @Transactional
    public void onFraudDetected(FraudDetectedEvent event) {
        log.info("Received FraudDetectedEvent for user: {}", event.getUsername());

        User user = userRepository.findByUsername(event.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + event.getUsername()));

        // Mark user as flagged for manual review
        user.setAccountLocked(true);

        // Penalize credibility score
        Double currentScore = user.getCredibilityScore();
        user.setCredibilityScore(Math.max(0.0, (currentScore != null ? currentScore : 0.0) - 10.0));

        userRepository.save(user);
        log.info("User {} flagged and penalized due to fraud detection.", event.getUsername());

        // Publish Status Change Event
        eventPublisher.publishEvent(
                UserStatusChangedEvent.builder()
                        .username(user.getUsername())
                        .active(false) // Locked means inactive/blocked for recipients
                        .build());
    }
}
