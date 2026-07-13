package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.entity.UserNotification;
import com.examplatform.modules.exam.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final UserNotificationRepository notificationRepository;

    // প্রতি ১০ মিনিটে চেক করবে
    @Scheduled(fixedRate = 600000)
    public void deleteExpiredNotifications() {
        List<UserNotification> expired = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        if (!expired.isEmpty()) {
            notificationRepository.deleteAll(expired);
        }
    }
}
