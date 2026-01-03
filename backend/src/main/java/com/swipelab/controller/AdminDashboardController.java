package com.swipelab.controller;

import com.swipelab.dto.request.CreateRecipientGroupRequest;
import com.swipelab.dto.request.CreateTaskRequest;
import com.swipelab.dto.request.UpdateRecipientGroupRequest;
import com.swipelab.dto.request.UpdateTaskRequest;
import com.swipelab.dto.response.*;
import com.swipelab.service.AdminDashboardService; // Rename service too if you like
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // ===== TASKS =====

    @GetMapping("/tasks")
    public List<TaskResponse> getTasks() {
        return adminDashboardService.getAllTasks();
    }

    @PostMapping("/tasks/create")
    public TaskResponse createTask(@RequestBody CreateTaskRequest request) {
        return adminDashboardService.createTask(request);
    }

    @PostMapping("/tasks/archive/{taskId}")
    public TaskResponse archiveTask(@PathVariable Long taskId) {
        return adminDashboardService.archiveTask(taskId);
    }

    @PutMapping("/tasks/{taskId}")
    public TaskResponse updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request
    ) {
        return adminDashboardService.updateTask(taskId, request);
    }

    @PostMapping("/tasks/{taskId}/activate")
    public TaskResponse activateTask(@PathVariable Long taskId) {
        return adminDashboardService.activateTask(taskId);
    }

    @PostMapping("/tasks/{taskId}/pause")
    public TaskResponse pauseTask(@PathVariable Long taskId) {
        return adminDashboardService.pauseTask(taskId);
    }

    @GetMapping("/tasks/{taskId}/analytics")
    public TaskAnalyticsResponse taskAnalytics(@PathVariable Long taskId) {
//        return adminDashboardService.getTaskAnalytics(taskId);
        throw new UnsupportedOperationException("Not implemented yet");
    }


    // ===== RECIPIENTS =====

    @GetMapping("/recipients")
    public List<RecipientGroupResponse> getRecipients() {
        return adminDashboardService.getRecipientGroups();
    }

    @PostMapping("/recipients/create")
    public RecipientGroupResponse createRecipients(
            @RequestBody CreateRecipientGroupRequest request
    ) {
        return adminDashboardService.createRecipientGroup(request);
    }

    @DeleteMapping("/recipients/{groupId}")
    public void deleteRecipients(@PathVariable Long groupId) {
        adminDashboardService.deleteRecipientGroup(groupId);
    }

    @PutMapping("/recipients/{groupId}/update")
    public RecipientGroupResponse updateRecipients(
            @PathVariable Long groupId,
            @RequestBody UpdateRecipientGroupRequest request
    ) {
        return adminDashboardService.updateRecipientGroup(groupId, request);
    }

    // ===== TAXONOMY =====

//    @GetMapping("/taxonomy")
//    public TaxonomyResponse getTaxonomy() {
//        throw new UnsupportedOperationException("Not implemented yet");
////        return adminDashboardService.getTaxonomy();
//    }
}
