package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> listNotifications(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        Page<Notification> notificationPage = notificationService.getNotifications(user.getId(), page, size);
        return ResponseEntity.ok(notificationPage);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestParam(required = false) String type,
                                            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return ResponseEntity.ok(0);
        }
        Long count;
        if (type != null && !type.isEmpty()) {
            count = notificationService.getUnreadCountByType(user.getId(), type);
        } else {
            count = notificationService.getUnreadCount(user.getId());
        }
        return ResponseEntity.ok(count);
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}