package com.example.smarteducationsystem_back.entity;

import lombok.Data;

@Data
public class DimCourse {
    private Integer id;
    private Integer collegeId;
    private String name;
    private Double credit;
}
