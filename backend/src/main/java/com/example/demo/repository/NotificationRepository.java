package com.example.demo.repository;

import com.example.demo.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdOrderByCreateTimeDesc(Long recipientId, Pageable pageable);
    Long countByRecipientIdAndIsReadFalse(Long recipientId);
    Long countByRecipientIdAndTypeAndIsReadFalse(Long recipientId, String type);
    // 新增：根据帖子ID删除所有通知
    @Transactional
    void deleteByPostId(Long postId);
}