package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NotificationResponse {
    private List<Notification> notifications;
    private int unreadCount;

    @Data
    @Builder
    public static class Notification {
        private String notificationId;
        private String title;
        private String body;
        private String type;
        private boolean isRead;
        private long sentAt;
    }
}