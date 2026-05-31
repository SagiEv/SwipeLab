package com.swipelab.tasks.infrastructure;

import com.swipelab.tasks.domain.Task;
import com.swipelab.tasks.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatus(TaskStatus status);

    long countByStatus(TaskStatus status);

    List<Task> findByCreatedBy(String username);

    boolean existsByCreatedByAndName(String createdBy, String name);

    long countByCreatedByAndCreatedAtAfter(String createdBy, java.time.LocalDateTime createdAt);

    @org.springframework.data.jpa.repository.Query(
        "SELECT DISTINCT t FROM Task t " +
        "LEFT JOIN t.recipientGroups rg " +
        "LEFT JOIN t.assignedUsernames au " +
        "WHERE t.status = :status AND " +
        "(au = :username OR rg IN :groupIds)"
    )
    org.springframework.data.domain.Page<Task> findAccessibleTasksForUser(
            @org.springframework.data.repository.query.Param("status") TaskStatus status,
            @org.springframework.data.repository.query.Param("username") String username,
            @org.springframework.data.repository.query.Param("groupIds") java.util.Collection<Long> groupIds,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Returns public ACTIVE tasks that the given user is NOT already assigned to
     * (neither directly by username nor via a recipient group membership).
     * Used exclusively by the "Explore Tasks" endpoint to prevent duplicates.
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT DISTINCT t FROM Task t " +
        "WHERE t.status = :status AND t.isPublic = true " +
        "AND NOT EXISTS (" +
        "  SELECT 1 FROM Task t2 " +
        "  LEFT JOIN t2.assignedUsernames au2 " +
        "  LEFT JOIN t2.recipientGroups rg2 " +
        "  WHERE t2.id = t.id AND (au2 = :username OR rg2 IN :groupIds)" +
        ")"
    )
    org.springframework.data.domain.Page<Task> findPublicTasksExcludingAssignedUser(
            @org.springframework.data.repository.query.Param("status") TaskStatus status,
            @org.springframework.data.repository.query.Param("username") String username,
            @org.springframework.data.repository.query.Param("groupIds") java.util.Collection<Long> groupIds,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Task t JOIN t.assignedUsernames au WHERE t.id = :id AND au = :username")
    boolean existsByIdAndUsernameInAssignedUsers(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("username") String username);

}
