package com.swipelab.tasks.application.port.in;

import com.swipelab.classification.application.port.out.TaskProvider;
import com.swipelab.exception.ResourceNotFoundException;
import com.swipelab.tasks.domain.Task;
import com.swipelab.tasks.infrastructure.TaskRepository;
import com.swipelab.tasks.application.port.out.TargetSpeciesProvider;
import com.swipelab.dto.response.TargetSpeciesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskProviderImpl implements TaskProvider {

    private final TaskRepository taskRepository;
    private final TargetSpeciesProvider targetSpeciesProvider;

    @Override
    @Transactional(readOnly = true)
    public TaskInfo getTaskInfo(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        
        List<TargetSpeciesResponse> species = targetSpeciesProvider.getSpeciesByIds(task.getTargetSpeciesIds());
        List<String> speciesNames = species.stream().map(TargetSpeciesResponse::getName).collect(Collectors.toList());

        return new TaskInfo(task.getId(), task.getQuestion(), task.getQuerySpecies(), speciesNames);
    }
}
