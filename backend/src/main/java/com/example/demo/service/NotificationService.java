package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Page<Notification> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Notification> pageResult = notificationRepository.findByRecipientIdOrderByCreateTimeDesc(userId, pageable);
        pageResult.forEach(notification -> {
            User sender = userRepository.findById(notification.getSenderId()).orElse(null);
            if (sender != null) {
                notification.setSenderUsername(sender.getUsername());
                notification.setSenderAvatarUrl(sender.getAvatarUrl());
            }
        });
        return pageResult;
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
    public Long getUnreadCountByType(Long userId, String type) {
        return notificationRepository.countByRecipientIdAndTypeAndIsReadFalse(userId, type);
    }
}