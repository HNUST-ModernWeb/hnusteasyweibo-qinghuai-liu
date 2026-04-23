package com.example.demo.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String content;

    private LocalDateTime createTime = LocalDateTime.now();

    @Transient
    private String username;
    @Transient
    private String avatarUrl;
    private String imageUrl;   // 评论图片URL，可为空
}