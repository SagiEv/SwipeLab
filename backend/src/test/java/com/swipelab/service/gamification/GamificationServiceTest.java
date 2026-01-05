package com.swipelab.service.gamification;

import com.swipelab.model.entity.Badge;
import com.swipelab.model.entity.User;
import com.swipelab.repository.BadgeRepository;
import com.swipelab.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private StreakService streakService;

    @InjectMocks
    private BadgeService badgeService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .points(0L)
                .currentStreak(0)
                .badges(new HashSet<>())
                .totalClassifications(0)
                .build();
    }

    @Test
    void testStreakInitialize() {
        user.setLastStreakUpdate(null);

        streakService.updateStreak(user);

        assertEquals(1, user.getCurrentStreak());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testStreakIncrement() {
        user.setLastStreakUpdate(LocalDateTime.now().minusDays(1));
        user.setCurrentStreak(5);

        streakService.updateStreak(user);

        assertEquals(6, user.getCurrentStreak());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testStreakReset() {
        user.setLastStreakUpdate(LocalDateTime.now().minusDays(2));
        user.setCurrentStreak(5);

        streakService.updateStreak(user);

        assertEquals(1, user.getCurrentStreak());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testSameDayNoUpdate() {
        user.setLastStreakUpdate(LocalDateTime.now());
        user.setCurrentStreak(5);

        streakService.updateStreak(user);

        assertEquals(5, user.getCurrentStreak());
        // Verify save is NOT called if no update needed (optimization check)
        // Adjust based on your implementation: currently implementations saves anyway
        // if we want updates timestamp?
        // Actually my implementation checks: if (today.equals(lastUpdateDate)) return;
        // So save should NOT be called.
        verify(userRepository, never()).save(user);
    }

    @Test
    void testAwardStreakBadge() {
        user.setCurrentStreak(3);
        when(badgeRepository.findByName("3 Day Streak"))
                .thenReturn(Optional.of(Badge.builder().name("3 Day Streak").build()));

        badgeService.checkForBadges(user);

        verify(userRepository, times(1)).save(user);
        assertEquals(1, user.getBadges().size());
        assertEquals("3 Day Streak", user.getBadges().iterator().next().getName());
    }
}
