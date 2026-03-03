package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.dto.ProfileDto;
import com.example.smarteducationsystem_back.entity.SysUser;
import com.example.smarteducationsystem_back.mapper.ProfileMapper;
import com.example.smarteducationsystem_back.mapper.SysUserMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import com.example.smarteducationsystem_back.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Profile", description = "基本信息接口")
public class ProfileController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ProfileMapper profileMapper;

    @GetMapping("/student/profile")
    @Operation(summary = "获取当前学生基本信息")
    @CheckRole("STUDENT")
    public Result<ProfileDto.StudentProfile> getStudentProfile() {
        Integer userId = CurrentUser.getUserId();
        SysUser user = sysUserMapper.findById(userId);
        if (user == null || user.getStudentId() == null) {
            return Result.error(400, "非学生或找不到对应的学生信息");
        }

        ProfileDto.StudentProfile profile = profileMapper.getStudentProfile(user.getStudentId());
        return Result.success(profile);
    }

    @GetMapping("/teacher/profile")
    @Operation(summary = "获取当前教师基本信息")
    @CheckRole("TEACHER")
    public Result<ProfileDto.TeacherProfile> getTeacherProfile() {
        Integer userId = CurrentUser.getUserId();
        SysUser user = sysUserMapper.findById(userId);
        if (user == null || user.getTeacherId() == null) {
            return Result.error(400, "非教师或找不到对应的教师信息");
        }

        ProfileDto.TeacherProfile profile = profileMapper.getTeacherProfile(user.getTeacherId());
        if (profile != null) {
            // Mock teachingYears based on a simple logic or default value
            profile.setTeachingYears(5); 
            List<String> courses = profileMapper.getTeacherCourses(user.getTeacherId());
            profile.setCourses(courses);
        }
        return Result.success(profile);
    }
}
