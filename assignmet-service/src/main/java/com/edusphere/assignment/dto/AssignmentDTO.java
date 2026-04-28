package com.edusphere.assignment.dto;
 
import jakarta.validation.constraints.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO {
 
    @NotNull(message = "Course ID required")
    private Long courseId;
 
    @NotBlank(message = "Title required")
    private String title;
 
    @NotBlank(message = "Question required")
    private String question;
 
    @NotNull(message = "Deadline required")
    private LocalDateTime deadline;
}