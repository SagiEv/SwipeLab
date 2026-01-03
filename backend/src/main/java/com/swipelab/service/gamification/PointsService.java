package com.swipelab.service.gamification;

import com.swipelab.model.entity.User;
import com.swipelab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final UserRepository userRepository;

    @Transactional
    public void addPoints(User user, int amount) {
        user.setPoints(user.getPoints() + amount);
        userRepository.save(user);
    }
}
