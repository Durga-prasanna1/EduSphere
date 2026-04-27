package com.edusphere.identity.dto;
 
import lombok.Data;
 
@Data
public class UpdateUserRequest {
    private String name;
    private String password;
    private String role;
    private Long departmentId;
}