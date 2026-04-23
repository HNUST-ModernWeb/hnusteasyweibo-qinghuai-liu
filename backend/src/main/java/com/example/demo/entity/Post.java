package com.example.demo.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String content;

    private LocalDateTime createTime = LocalDateTime.now();

    @Transient
    private String username;      // 发帖人用户名
    @Transient
    private String avatarUrl;     // 发帖人头像
    @Transient
    private Long commentCount;    // 评论数
    @Transient
    private Long likeCount;
    @Transient
    private Boolean liked;  // 当前登录用户是否点赞
    private String imageUrl;   // 帖子图片URL，可为空
}