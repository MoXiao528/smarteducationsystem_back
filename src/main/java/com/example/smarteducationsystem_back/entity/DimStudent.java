package com.example.smarteducationsystem_back.entity;

import lombok.Data;

@Data
public class DimStudent {
    private Integer id;
    private String studentNo;
    private String name;
    private Integer collegeId;
    private Integer majorId;
    private Integer gradeId;
    private Integer classId;
}
