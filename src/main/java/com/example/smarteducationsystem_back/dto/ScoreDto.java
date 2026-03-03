package com.example.smarteducationsystem_back.dto;

import lombok.Data;

public class ScoreDto {

    @Data
    public static class Req {
        private Integer semesterId;
        private Integer collegeId;
        private Integer majorId;
        private Integer gradeId;
        private Integer classId;
        private Integer courseId;
        private String studentKey; // 姓名或学号模糊匹配
        private Integer studentId; // DataScope: 强制本人的studentId
        private Integer teacherId; // DataScope: 强制本人的teacherId
        private Double scoreMin;
        private Double scoreMax;
        
        private Integer page = 1;
        private Integer size = 20;
        private String sort; // 格式如 score,desc
    }

    @Data
    public static class Item {
        private Long id;
        private Integer semesterId;
        private Integer collegeId;
        private Integer majorId;
        private Integer gradeId;
        private Integer classId;
        private Integer studentId;
        private String studentNo;
        private String studentName;
        private Integer courseId;
        private String courseName;
        private Integer teacherId;
        private String teacherName;
        private Double score;
        private Integer isAbsent;
    }
}
