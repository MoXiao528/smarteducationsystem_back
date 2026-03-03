package com.example.smarteducationsystem_back.dto;

import lombok.Data;
import java.util.List;

public class AuthDto {

    @Data
    public static class LoginReq {
        private String username;
        private String password;
    }

    @Data
    public static class LoginResp {
        private String token;
    }

    @Data
    public static class MeResp {
        private Integer id;
        private String name;
        private List<String> roles;
        private List<String> permissions;
        private DataScope dataScope;
    }

    @Data
    public static class DataScope {
        private String type; // ALL | COLLEGE | TEACHING | SELF
        private Integer collegeId;
        private List<Integer> courseIds;
        private List<Integer> classIds;
        private Integer studentId;
    }
}
