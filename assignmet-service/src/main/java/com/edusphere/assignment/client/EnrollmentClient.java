package com.edusphere.assignment.client;
 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
 
@Component
@RequiredArgsConstructor
public class EnrollmentClient {
 
    private final WebClient webClient;
 
    private static final String BASE_URL = "http://localhost:8082";
    public boolean isUserEnrolled(Long userId, Long courseId, String role, String token) {
    	 
    	return webClient.get()
    	        .uri(BASE_URL + "/api/enrollments/check?userId="
    	                + userId + "&courseId=" + courseId + "&role=" + role)
    	        .header("Authorization", "Bearer " + token)
    	        .retrieve()
    	        .bodyToMono(Boolean.class)
    	        .block();
    }
    // ✅ STUDENT CHECK
//    public boolean isStudentEnrolled(Long studentId, Long courseId, String token) {
// 
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .scheme("http")
//                        .host("localhost")
//                        .port(8082)
//                        .path("/api/enrollments/check")
//                        .queryParam("studentId", studentId)
//                        .queryParam("courseId", courseId)
//                        .build())
//                .header("Authorization", token)
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .block();
//    }
// 
//    // ✅ TEACHER CHECK
//    public boolean isTeacherAssigned(Long teacherId, Long courseId, String token) {
// 
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .scheme("http")
//                        .host("localhost")
//                        .port(8082)
//                        .path("/api/enrollments/teacher-check") // 🔥 FIXED
//                        .queryParam("teacherId", teacherId)
//                        .queryParam("courseId", courseId)
//                        .build())
//                .header("Authorization", token)
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .block();
//    }
}