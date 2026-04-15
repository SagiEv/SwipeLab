package com.swipelab.integration.stardbi;

import com.swipelab.classification.events.ClassificationSubmittedEvent;
import com.swipelab.integration.stardbi.dto.ExternalLabelDto;
import com.swipelab.tasks.domain.Task;
import com.swipelab.tasks.infrastructure.TaskRepository;
import com.swipelab.users.domain.User;
import com.swipelab.users.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StardbiClassificationEventListener {

    private final StardbiClient stardbiClient;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "classification-events", groupId = "stardbi-integration-group")
    @Transactional(readOnly = true)
    public void handleClassificationEvent(ClassificationSubmittedEvent event) {
        try {
            log.info("Received classification event for task {}", event.getTaskId());

            // 1. Fetch task to check if it's a STARdbi task
            Optional<Task> taskOpt = taskRepository.findById(event.getTaskId());
            if (taskOpt.isEmpty() || !"STARDBI".equals(taskOpt.get().getSourceSystem())) {
                return; // Not an external task
            }

            // 2. Fetch user to get external ID mapping
            Optional<User> userOpt = userRepository.findByUsername(event.getUsername());
            
            if (userOpt.isPresent()) {
                ExternalLabelDto label = ExternalLabelDto.builder()
                        .swipeLabUserId(1L) // FIXME: User ID is string username, API.md expects integer
                        .userGrade(event.isCorrect() ? 3 : 1) // Mapping logic
                        .boxId(event.getImageId()) // In our domain imageId might correspond to the bounding box
                        .imageId(1L) // FIXME: Provide correct parent image ID 
                        .speciesId(1L) // Needs mapping from species string to species_id
                        .build();

                stardbiClient.postLabel(label);
                log.info("Successfully synced label to STARdbi for box {}", event.getImageId());
            }

        } catch (Exception e) {
            log.error("Failed to process classification event for STARdbi sync", e);
            // In a real system, we might want to leverage dead-letter queues here.
        }
    }
}
