package com.example.smarteducationsystem_back.entity;

import lombok.Data;

@Data
public class FactScore {
    private Long id;
    private Integer semesterId;
    private Integer studentId;
    private Integer courseId;
    private Integer teacherId;
    private Double score;
    private Integer isAbsent;
    
    private Integer collegeId;
    private Integer majorId;
    private Integer gradeId;
    private Integer classId;
}
