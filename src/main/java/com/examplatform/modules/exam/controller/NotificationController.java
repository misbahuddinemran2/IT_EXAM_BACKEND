package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.NotificationResponse;
import com.examplatform.modules.exam.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User Notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get Notifications",
            description = "Retrieve all notifications for a user"
    )
    @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully")
    public ResponseEntity<NotificationResponse> getNotifications(
            @PathVariable String userId) {

        NotificationResponse notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{notificationId}/read")
    @Operation(
            summary = "Mark as Read",
            description = "Mark a notification as read"
    )
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String notificationId) {

        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}