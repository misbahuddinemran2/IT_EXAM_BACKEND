package com.examplatform.modules.notification.service;

import com.examplatform.modules.notification.entity.UserNotification;
import com.examplatform.modules.notification.enums.NotificationType;
import com.examplatform.modules.notification.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserNotificationRepository notificationRepository;

    /**
     * একজন ইউজারকে notification পাঠানো (যেকোনো মডিউল থেকে কল করা যাবে —
     * Written exam, MCQ exam, Live exam, subscription, announcement ইত্যাদি)
     */
    public void notifyUser(String userId, NotificationType type, String title, String body, Map<String, Object> metadata) {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setMetadata(metadata);
        notificationRepository.save(notification);
    }

    /**
     * একসাথে অনেক ইউজারকে একই notification পাঠানো (যেমন: exam publish হলে সব অংশগ্রহণকারীকে)
     */
    public void notifyUsers(List<String> userIds, NotificationType type, String title, String body, Map<String, Object> metadata) {
        for (String userId : userIds) {
            notifyUser(userId, type, title, body, metadata);
        }
    }
}
