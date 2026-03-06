package com.example.smarteducationsystem_back.dto;

import lombok.Data;
import java.util.List;

public class ProfileDto {

    @Data
    public static class StudentProfile {
        private String name;
        private String studentNo;
        private Integer collegeId;
        private String collegeName;
        private Integer majorId;
        private String majorName;
        private Integer gradeId;
        private String gradeName;
        private Integer classId;
        private String className;
    }

    @Data
    public static class TeacherProfile {
        private String teacherNo;
        private String name;
        private Integer teachingYears;
        private String title;
        private String collegeName;
        private List<String> courses;
    }
}
