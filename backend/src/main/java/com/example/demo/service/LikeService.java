package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class LikeService {
    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public LikeService(PostLikeRepository likeRepository, PostRepository postRepository,
                       UserRepository userRepository, NotificationRepository notificationRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        Optional<PostLike> existing = likeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false; // 取消点赞
        } else {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            likeRepository.save(like);

            // 给帖子作者发送点赞通知（排除自己点赞自己）
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null && !post.getUserId().equals(userId)) {
                Notification notification = new Notification();
                notification.setRecipientId(post.getUserId());
                notification.setSenderId(userId);
                notification.setType("LIKE");
                notification.setPostId(postId);
                notification.setIsRead(false);
                notificationRepository.save(notification);
            }
            return true;
        }
    }

    public Long getLikeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    public boolean isLiked(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }
}