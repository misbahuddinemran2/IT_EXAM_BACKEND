package com.examplatform.modules.notification.controller;

import com.examplatform.modules.notification.entity.UserNotification;
import com.examplatform.modules.notification.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class StudentNotificationController {

    private final UserNotificationRepository notificationRepository;

    @GetMapping("/my")
    public List<UserNotification> getMyNotifications(Authentication auth) {
        String userId = auth.getName(); // ⚠️ নিচে নোট দেখুন — userId resolve করার সঠিক পদ্ধতি
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(Authentication auth) {
        String userId = auth.getName();
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable String notificationId) {
        UserNotification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification পাওয়া যায়নি"));
        n.setRead(true);
        n.setReadAt(java.time.LocalDateTime.now());
        notificationRepository.save(n);
    }

    @PatchMapping("/read-all")
    public void markAllAsRead(Authentication auth) {
        notificationRepository.markAllAsRead(auth.getName());
    }
}
