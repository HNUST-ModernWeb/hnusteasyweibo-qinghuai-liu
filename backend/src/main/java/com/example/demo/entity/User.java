package com.example.demo.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    private LocalDateTime registerTime = LocalDateTime.now();

    // 新增角色字段，默认普通用户
    private String role = "USER";   // "USER" 或 "ADMIN"
}