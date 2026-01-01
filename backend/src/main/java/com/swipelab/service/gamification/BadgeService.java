package com.swipelab.service.gamification;

import com.swipelab.model.entity.Badge;
import com.swipelab.model.entity.User;
import com.swipelab.repository.BadgeRepository;
import com.swipelab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;

    private static final String BADGE_FIRST_SWIPE = "First Swipe";
    private static final String BADGE_10_SWIPES = "10 Swipes";
    private static final String BADGE_100_SWIPES = "100 Swipes";

    @Transactional
    public void checkForBadges(User user) {
        int totalSwipes = user.getTotalClassifications();

        if (totalSwipes == 1) {
            awardBadge(user, BADGE_FIRST_SWIPE);
        } else if (totalSwipes == 10) {
            awardBadge(user, BADGE_10_SWIPES);
        } else if (totalSwipes == 100) {
            awardBadge(user, BADGE_100_SWIPES);
        }
    }

    private void awardBadge(User user, String badgeName) {
        Optional<Badge> badgeOpt = badgeRepository.findByName(badgeName);
        if (badgeOpt.isPresent()) {
            Badge badge = badgeOpt.get();
            if (!user.getBadges().contains(badge)) {
                user.getBadges().add(badge);
                userRepository.save(user);
            }
        }
    }
}
