package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/like")
public class LikeController {
    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleLike(@RequestParam Long postId, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return ResponseEntity.status(401).body("请先登录");
        }
        boolean liked = likeService.toggleLike(postId, user.getId());
        Map<String, Object> res = new HashMap<>();
        res.put("liked", liked);
        res.put("likeCount", likeService.getLikeCount(postId));
        return ResponseEntity.ok(res);
    }
}