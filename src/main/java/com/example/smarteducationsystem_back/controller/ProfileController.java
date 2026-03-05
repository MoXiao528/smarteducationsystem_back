package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.PageResult;
import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.dto.ProfileDto;
import com.example.smarteducationsystem_back.entity.SysUser;
import com.example.smarteducationsystem_back.mapper.ProfileMapper;
import com.example.smarteducationsystem_back.mapper.SysUserMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import com.example.smarteducationsystem_back.security.CurrentUser;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @CheckRole({"STUDENT"})
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
    @CheckRole({"TEACHER"})
    public Result<ProfileDto.TeacherProfile> getTeacherProfile() {
        Integer userId = CurrentUser.getUserId();
        SysUser user = sysUserMapper.findById(userId);
        if (user == null || user.getTeacherId() == null) {
            return Result.error(400, "非教师或找不到对应的教师信息");
        }

        ProfileDto.TeacherProfile profile = profileMapper.getTeacherProfile(user.getTeacherId());
        if (profile != null) {
            profile.setTeachingYears(5);
            List<String> courses = profileMapper.getTeacherCourses(user.getTeacherId());
            profile.setCourses(courses);
        }
        return Result.success(profile);
    }

    @GetMapping("/profile/students")
    @Operation(summary = "获取学生列表 (带数据权限+筛选+分页)")
    @CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN", "COLLEGE_ADMIN", "TEACHER"})
    public Result<PageResult<ProfileDto.StudentProfile>> getStudentList(
            @RequestParam(required = false) Integer collegeId,
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) Integer gradeId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) String studentKey,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Integer userId = CurrentUser.getUserId();
        SysUser user = sysUserMapper.findById(userId);

        // DataScope: 强制隔离
        Integer filterTeacherId = null;
        if ("COLLEGE_ADMIN".equals(user.getRoleType())) {
            collegeId = user.getCollegeId();
        } else if ("TEACHER".equals(user.getRoleType())) {
            filterTeacherId = user.getTeacherId();
        }

        PageHelper.startPage(page, size);
        List<ProfileDto.StudentProfile> list = profileMapper.getStudentList(
                collegeId, majorId, gradeId, classId, filterTeacherId, studentKey);
        Page<ProfileDto.StudentProfile> pageInfo = (Page<ProfileDto.StudentProfile>) list;

        return Result.success(new PageResult<>(list, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal()));
    }

    @GetMapping("/profile/teachers")
    @Operation(summary = "获取教师列表 (带数据权限)")
    @CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN", "COLLEGE_ADMIN"})
    public Result<List<ProfileDto.TeacherProfile>> getTeacherList() {
        Integer userId = CurrentUser.getUserId();
        SysUser user = sysUserMapper.findById(userId);
        
        Integer filterCollegeId = null;

        if ("COLLEGE_ADMIN".equals(user.getRoleType())) {
            filterCollegeId = user.getCollegeId();
        }

        List<ProfileDto.TeacherProfile> list = profileMapper.getTeacherList(filterCollegeId);
        return Result.success(list);
    }
}

