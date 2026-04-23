package com.example.demo.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientId;        // 接收者用户ID

    @Column(nullable = false)
    private Long senderId;           // 触发者用户ID

    @Column(nullable = false)
    private String type;             // "LIKE" 或 "COMMENT"

    private Long postId;             // 关联帖子ID

    private String content;          // 评论内容（仅评论通知需要）

    private Boolean isRead = false;  // 是否已读

    private LocalDateTime createTime = LocalDateTime.now();

    // 以下为 Transient 字段，用于前端展示
    @Transient
    private String senderUsername;
    @Transient
    private String senderAvatarUrl;
}