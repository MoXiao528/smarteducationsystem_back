package com.example.smarteducationsystem_back.entity;

import lombok.Data;

@Data
public class FactEnroll {
    private Long id;
    private Integer semesterId;
    private Integer courseId;
    private Integer teacherId;
    private Integer studentId;
    private Integer collegeId;
    private Integer majorId;
    private Integer gradeId;
    private Integer isDrop;
}
