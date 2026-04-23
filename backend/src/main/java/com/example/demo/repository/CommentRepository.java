package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostIdOrderByCreateTimeDesc(Long postId, Pageable pageable);
    Long countByPostId(Long postId);

    // 新增：根据帖子ID删除所有评论
    @Transactional
    void deleteByPostId(Long postId);
}