package com.swipelab.service.analytics;

import com.swipelab.dto.response.DashboardStatsResponse;
import com.swipelab.model.enums.TaskStatus;
import com.swipelab.repository.ClassificationRepository;
import com.swipelab.repository.ImageRepository;
import com.swipelab.repository.TaskRepository;
import com.swipelab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;
    private final ClassificationRepository classificationRepository;
    private final TaskRepository taskRepository;
    private final ImageRepository imageRepository;

    public DashboardStatsResponse getGlobalStats() {
        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalSwipes(classificationRepository.count())
                .activeTasks(taskRepository.findByStatus(TaskStatus.ACTIVE).size())
                .totalImages(imageRepository.count())
                .build();
    }
}
