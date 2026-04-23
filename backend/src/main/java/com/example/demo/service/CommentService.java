package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Notification;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;          // 新增
    private final NotificationRepository notificationRepository; // 新增

    @Value("${file.upload-dir}")
    private String uploadDir;
    // 修改构造器，注入所需依赖
    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          PostRepository postRepository,
                          NotificationRepository notificationRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Comment createComment(Long userId, Long postId, String content, MultipartFile image) throws IOException {
        if (content == null || content.trim().isEmpty() || content.length() > 200) {
            throw new RuntimeException("评论内容不符合要求");
        }

        // 1. 先保存评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setContent(content.trim());

        // 👇 新增图片处理逻辑
        if (image != null && !image.isEmpty()) {
            String originalFilename = image.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + ext;
            Path targetPath = Paths.get(uploadDir, newFilename);
            Files.copy(image.getInputStream(), targetPath);
            comment.setImageUrl("/uploads/" + newFilename);
        }

        Comment savedComment = commentRepository.save(comment);

        // 2. 获取帖子信息，用于发送通知
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        // 3. 如果评论者不是帖子作者，则发送通知
        if (!post.getUserId().equals(userId)) {
            Notification notification = new Notification();
            notification.setRecipientId(post.getUserId()); // 帖子作者
            notification.setSenderId(userId);              // 评论者
            notification.setType("COMMENT");
            notification.setPostId(postId);
            notification.setContent(content);              // 评论内容
            notification.setIsRead(false);
            notificationRepository.save(notification);
        }

        return savedComment;
    }

    // 原有的分页查询方法（保持不变或稍后补充）
    public Page<Comment> getCommentsPage(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Comment> commentPage = commentRepository.findByPostIdOrderByCreateTimeDesc(postId, pageable);
        commentPage.forEach(comment -> {
            User user = userRepository.findById(comment.getUserId()).orElse(null);
            if (user != null) {
                comment.setUsername(user.getUsername());
                comment.setAvatarUrl(user.getAvatarUrl());
            }
        });
        return commentPage;
    }
}