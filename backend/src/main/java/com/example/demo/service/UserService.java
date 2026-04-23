package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final String uploadDir;

    public UserService(UserRepository userRepository,
                       @Value("${file.upload-dir}") String uploadDir) {
        this.userRepository = userRepository;
        this.uploadDir = uploadDir;
        // 确保上传目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    public User register(String username, String password, MultipartFile avatar) throws IOException {
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 实际项目中应加密

        // 保存头像
        if (avatar != null && !avatar.isEmpty()) {
            String originalFilename = avatar.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + ext;
            Path targetPath = Paths.get(uploadDir, newFilename);
            Files.copy(avatar.getInputStream(), targetPath);
            user.setAvatarUrl("/uploads/" + newFilename);
        } else {
            user.setAvatarUrl("/uploads/default.png"); // 默认头像
        }
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateProfile(Long userId, String newUsername, String newPassword, MultipartFile avatar) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        if (newUsername != null && !newUsername.equals(user.getUsername())) {
            if (userRepository.findByUsername(newUsername) != null) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(newUsername);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }
        if (avatar != null && !avatar.isEmpty()) {
            String originalFilename = avatar.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + ext;
            Path targetPath = Paths.get(uploadDir, newFilename);
            Files.copy(avatar.getInputStream(), targetPath);
            user.setAvatarUrl("/uploads/" + newFilename);
        }
        return userRepository.save(user);
    }
}