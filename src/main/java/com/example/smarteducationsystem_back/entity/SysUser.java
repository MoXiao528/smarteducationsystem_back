package com.example.smarteducationsystem_back.entity;

import lombok.Data;

@Data
public class SysUser {
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String roleType;
    private Integer collegeId;
    private Integer studentId;
    private Integer teacherId;
}
