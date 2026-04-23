package com.example.demo.controller;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestParam String content,
                                        HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        try {
            Post post = postService.createPost(user.getId(), content);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listPosts(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Long currentUserId = user != null ? user.getId() : null;
        Page<Post> postPage = postService.getPostsPage(page, size, currentUserId);
        return ResponseEntity.ok(postPage);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long id,
                                           HttpSession session) {
        try {
            User user = (User) session.getAttribute("currentUser");
            Long currentUserId = user != null ? user.getId() : null;
            Post post = postService.getPostDetail(id, currentUserId);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/admin/post/{id}")
    public ResponseEntity<?> deletePostByAdmin(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return ResponseEntity.status(401).body("未登录");
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body("权限不足");
        }
        try {
            postService.deletePostByAdmin(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}