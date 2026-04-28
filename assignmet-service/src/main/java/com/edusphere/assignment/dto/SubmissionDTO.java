package com.edusphere.assignment.dto;
 
import jakarta.validation.constraints.*;
import lombok.*;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDTO {
 
    @NotNull(message = "Assignment ID required")
    private Long assignmentId;
 
    @NotNull(message = "Student ID required")
    private Long studentId;
 
    // ❌ REMOVE fileUrl
}