package com.edusphere.identity.service.impl;
 
import com.edusphere.identity.dto.UpdateUserRequest;
import com.edusphere.identity.entity.Role;
import com.edusphere.identity.entity.User;
import com.edusphere.identity.repository.UserRepository;
import com.edusphere.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
 
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
 
    private final UserRepository userRepository;
 
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByIsDeletedFalse();
    }
 
    @Override
    public User updateUser(Long id, UpdateUserRequest request) {
 
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 
        if (request.getName() != null) {
            user.setName(request.getName());
        }
 
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword()); // ⚠️ Later encode
        }
 
        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }
 
        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
        }
 
        return userRepository.save(user);
    }
 
    @Override
    public void softDeleteUser(Long id) {
 
        User user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
 
        user.setDeleted(true); // ✅ Soft delete
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}