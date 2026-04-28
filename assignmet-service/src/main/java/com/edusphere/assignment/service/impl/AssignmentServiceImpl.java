package com.edusphere.assignment.service.impl;
 
import com.edusphere.assignment.dto.AssignmentDTO;
import com.edusphere.assignment.entity.Assignment;
import com.edusphere.assignment.repository.AssignmentRepository;
import com.edusphere.assignment.security.JwtUtil;
import com.edusphere.assignment.service.AssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {
 
    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceImpl.class);
 
    private final AssignmentRepository assignmentRepository;
    private final JwtUtil jwtUtil;
    private final HttpServletRequest request;
 
    // ================= TOKEN =================
    private String extractToken() {
        String header = request.getHeader("Authorization");
 
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
 
        throw new RuntimeException("Missing Authorization");
    }
 
    // ================= CREATE =================
    @Override
    public Assignment createAssignment(AssignmentDTO dto) {
 
        String token = extractToken();
 
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
 
        if (!role.equals("TEACHER")) {
            throw new RuntimeException("Only teachers can create assignments");
        }
 
        Assignment assignment = Assignment.builder()
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .question(dto.getQuestion())
                .deadline(dto.getDeadline())
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
 
        return assignmentRepository.save(assignment);
    }
 
    // ================= GET =================
    @Override
    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }
 
    // ================= UPDATE =================
    @Override
    public Assignment updateAssignment(Long id, AssignmentDTO dto) {
 
        String token = extractToken();
 
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
 
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
 
        if (!role.equals("TEACHER") || !assignment.getCreatedBy().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
 
        assignment.setTitle(dto.getTitle());
        assignment.setQuestion(dto.getQuestion());
        assignment.setDeadline(dto.getDeadline());
        assignment.setUpdatedAt(LocalDateTime.now());
 
        return assignmentRepository.save(assignment);
    }
 
    // ================= DELETE =================
    @Override
    public void deleteAssignment(Long id) {
 
        String token = extractToken();
 
        String role = jwtUtil.extractRole(token);
        Long userId = jwtUtil.extractUserId(token);
 
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
 
        if (role.equals("TEACHER") && !assignment.getCreatedBy().equals(userId)) {
            throw new RuntimeException("Cannot delete others assignments");
        }
 
        if (!role.equals("TEACHER") && !role.equals("ADMIN")) {
            throw new RuntimeException("Unauthorized");
        }
 
        assignmentRepository.delete(assignment);
    }
 
    // ================= FILE SAVE =================
    private String saveFile(MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/questions";
 
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
 
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
 
            String path = uploadDir + "/" + fileName;
 
            file.transferTo(new File(path));
 
            return path;
 
        } catch (Exception e) {
            throw new RuntimeException("File upload failed");
        }
    }
 
    // ================= DOWNLOAD =================
//    @Override
//    public ResponseEntity<Resource> downloadQuestionFile(Long id) {
// 
//        Assignment assignment = assignmentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Assignment not found"));
// 
//        if (assignment.getQuestionFilePath() == null) {
//            throw new RuntimeException("No file available");
//        }
// 
//        try {
//            Path path = Paths.get(assignment.getQuestionFilePath());
// 
//            Resource resource = new UrlResource(path.toUri());
// 
//            return ResponseEntity.ok()
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .header(HttpHeaders.CONTENT_DISPOSITION,
//                            "attachment; filename=\"" + path.getFileName() + "\"")
//                    .body(resource);
// 
//        } catch (Exception e) {
//            throw new RuntimeException("File not found");
//        }
//    }
}