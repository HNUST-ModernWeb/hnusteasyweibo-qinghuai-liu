package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreateTimeDesc(Pageable pageable);

    // 新增：根据用户ID分页查询帖子
    Page<Post> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
}