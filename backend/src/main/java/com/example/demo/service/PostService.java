package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeService likeService;
    private final PostLikeRepository likeRepository;           // 新增
    private final NotificationRepository notificationRepository; // 新增

    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       CommentRepository commentRepository,
                       LikeService likeService,
                       PostLikeRepository likeRepository,
                       NotificationRepository notificationRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeService = likeService;
        this.likeRepository = likeRepository;
        this.notificationRepository = notificationRepository;
    }

    public Post createPost(Long userId, String content) {
        if (content == null || content.trim().isEmpty() || content.length() > 200) {
            throw new RuntimeException("内容不符合要求");
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content.trim());
        return postRepository.save(post);
    }

    public Page<Post> getPostsPage(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Post> postPage = postRepository.findAllByOrderByCreateTimeDesc(pageable);
        postPage.forEach(post -> {
            User user = userRepository.findById(post.getUserId()).orElse(null);
            if (user != null) {
                post.setUsername(user.getUsername());
                post.setAvatarUrl(user.getAvatarUrl());
            }
            Long commentCount = commentRepository.countByPostId(post.getId());
            post.setCommentCount(commentCount);
            post.setLikeCount(likeService.getLikeCount(post.getId()));
            if (currentUserId != null) {
                post.setLiked(likeService.isLiked(post.getId(), currentUserId));
            } else {
                post.setLiked(false);
            }
        });
        return postPage;
    }

    public Post getPostDetail(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        User user = userRepository.findById(post.getUserId()).orElse(null);
        if (user != null) {
            post.setUsername(user.getUsername());
            post.setAvatarUrl(user.getAvatarUrl());
        }
        post.setCommentCount(commentRepository.countByPostId(postId));
        post.setLikeCount(likeService.getLikeCount(postId));
        if (currentUserId != null) {
            post.setLiked(likeService.isLiked(postId, currentUserId));
        } else {
            post.setLiked(false);
        }
        return post;
    }

    public Page<Post> getPostsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Post> postPage = postRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        postPage.forEach(post -> {
            User user = userRepository.findById(post.getUserId()).orElse(null);
            if (user != null) {
                post.setUsername(user.getUsername());
                post.setAvatarUrl(user.getAvatarUrl());
            }
            post.setCommentCount(commentRepository.countByPostId(post.getId()));
            post.setLikeCount(likeService.getLikeCount(post.getId()));
        });
        return postPage;
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除");
        }
        commentRepository.deleteByPostId(postId);
        likeRepository.deleteByPostId(postId);
        notificationRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }
    @Transactional
    public void deletePostByAdmin(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        // 管理员可删除任意帖子，无需检查 userId
        commentRepository.deleteByPostId(postId);
        likeRepository.deleteByPostId(postId);
        notificationRepository.deleteByPostId(postId);
        postRepository.delete(post);
    }
}