package com.edusphere.assignment.controller;
 
import com.edusphere.assignment.entity.Submission;
import com.edusphere.assignment.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {
 
    private final SubmissionService submissionService;
    private final HttpServletRequest request;
 
    // ✅ SUBMIT (student)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Submission submit(
            @RequestParam Long assignmentId,
            @RequestPart MultipartFile file) {
 
        String token = request.getHeader("Authorization");
        return submissionService.submitAssignment(assignmentId, file, token);
    }
 
    // ✅ GET SUBMISSIONS (teacher/admin)
    @GetMapping("/{assignmentId}")
    public List<Submission> getSubmissions(@PathVariable Long assignmentId) {
        String token = request.getHeader("Authorization");
        return submissionService.getSubmissionsByAssignment(assignmentId, token);
    }
 
    // ✅ UPDATE (student)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Submission update(
            @PathVariable Long id,
            @RequestPart MultipartFile file) {
 
        String token = request.getHeader("Authorization");
        return submissionService.updateSubmission(id, file, token);
    }
 
    // ✅ DOWNLOAD FILE (teacher/student)
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
     
        Resource file = submissionService.downloadFile(id);
     
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
 
    // ✅ GRADE (teacher)
    @PutMapping("/grade/{id}")
    public Submission grade(
            @PathVariable Long id,
            @RequestParam Integer marks) {
 
        String token = request.getHeader("Authorization");
        return submissionService.gradeSubmission(id, marks, token);
    }
}