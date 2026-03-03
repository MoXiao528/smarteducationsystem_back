package com.example.smarteducationsystem_back.dto;

import lombok.Data;
import java.util.List;

public class ProfileDto {

    @Data
    public static class StudentProfile {
        private String name;
        private String studentNo;
        private String collegeName;
        private String majorName;
        private String gradeName;
        private String className;
    }

    @Data
    public static class TeacherProfile {
        private String name;
        private Integer teachingYears;
        private String title;
        private List<String> courses;
    }
}
