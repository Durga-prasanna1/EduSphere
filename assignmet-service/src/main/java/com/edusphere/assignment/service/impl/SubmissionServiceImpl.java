package com.edusphere.assignment.service.impl;
 
import com.edusphere.assignment.client.EnrollmentClient;
import com.edusphere.assignment.entity.Assignment;
import com.edusphere.assignment.entity.Submission;
import com.edusphere.assignment.repository.AssignmentRepository;
import com.edusphere.assignment.repository.SubmissionRepository;
import com.edusphere.assignment.security.JwtUtil;
import com.edusphere.assignment.service.SubmissionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
 
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
 
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final JwtUtil jwtUtil;
    private final EnrollmentClient enrollmentClient;
 
    private static final String UPLOAD_DIR = "uploads/";
 
    // ===================== SUBMIT =====================
    @Override
    public Submission submitAssignment(Long assignmentId, MultipartFile file, String token) {
 
        // ✅ FIX 1: Safe token handling
        String rawToken = extractToken(token);
 
        String role = jwtUtil.extractRole(rawToken);
        Long studentId = jwtUtil.extractUserId(rawToken);
 
        if (!role.equals("STUDENT")) {
            throw new RuntimeException("Only students can submit assignments");
        }
 
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
 
        // ✅ FIX 2: Pass Bearer token to client
        boolean enrolled = enrollmentClient.isUserEnrolled(
                studentId,
                assignment.getCourseId(),
                role,
                rawToken
        );
 
        if (!enrolled) {
            throw new RuntimeException("Student is not enrolled in this course");
        }
 
        String filePath = saveFile(file);
 
        Submission submission = Submission.builder()
                .assignmentId(assignmentId)
                .studentId(studentId)
                .filePath(filePath)
                .status("SUBMITTED")
                .submittedAt(LocalDateTime.now())
                .build();
 
        return submissionRepository.save(submission);
    }
 
    // ===================== GET =====================
    @Override
    public List<Submission> getSubmissionsByAssignment(Long assignmentId, String token) {
 
        String rawToken = extractToken(token);
 
        String role = jwtUtil.extractRole(rawToken);
 
        if (!role.equals("TEACHER") && !role.equals("ADMIN")) {
            throw new RuntimeException("Only teachers/admin can view submissions");
        }
 
        return submissionRepository.findByAssignmentId(assignmentId);
    }
 
    // ===================== UPDATE =====================
    @Override
    public Submission updateSubmission(Long id, MultipartFile file, String token) {
 
        String rawToken = extractToken(token);
 
        Long studentId = jwtUtil.extractUserId(rawToken);
 
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
 
        if (!submission.getStudentId().equals(studentId)) {
            throw new RuntimeException("You can only update your own submission");
        }
 
        String filePath = saveFile(file);
 
        submission.setFilePath(filePath);
        submission.setSubmittedAt(LocalDateTime.now());
 
        return submissionRepository.save(submission);
    }
 
    // ===================== GRADE =====================
    @Override
    public Submission gradeSubmission(Long submissionId, Integer marks, String token) {
 
        String rawToken = extractToken(token);
 
        String role = jwtUtil.extractRole(rawToken);
        Long teacherId = jwtUtil.extractUserId(rawToken);
 
        if (!role.equals("TEACHER")) {
            throw new RuntimeException("Only teachers can grade submissions");
        }
 
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
 
        Assignment assignment = assignmentRepository.findById(submission.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
 
        // ✅ FIX 3: Pass Bearer token
        boolean allowed = enrollmentClient.isUserEnrolled(
                teacherId,
                assignment.getCourseId(),
                role,
                "Bearer " + rawToken
        );
 
        if (!allowed) {
            throw new RuntimeException("Teacher is not assigned to this course");
        }
 
        submission.setMarks(marks);
        submission.setStatus("GRADED");
 
        return submissionRepository.save(submission);
    }
 
    // ===================== FILE SAVE =====================
    private String saveFile(MultipartFile file) {
    	 
        try {
            // ✅ FIX 1: Create uploads folder properly
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
     
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
     
            // ✅ FIX 2: Clean filename
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
     
            // ✅ FIX 3: Full path
            String filePath = uploadDir + File.separator + fileName;
     
            // ✅ FIX 4: Save file
            file.transferTo(new File(filePath));
     
            return filePath;
     
        } catch (IOException e) {
            e.printStackTrace(); // 🔥 important for debugging
            throw new RuntimeException("File upload failed", e);
        }
    }
    
    @Override
    public Resource downloadFile(Long submissionId) {
     
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
     
        try {
            Path path = Paths.get(submission.getFilePath()).toAbsolutePath();
            Resource resource = new UrlResource(path.toUri());
     
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }
     
            return resource;
     
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file", e);
        }
    }
 
    // ===================== TOKEN HELPER =====================
    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}