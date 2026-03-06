package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.dto.AuthDto;
import com.example.smarteducationsystem_back.dto.ProfileDto;
import com.example.smarteducationsystem_back.entity.SysUser;
import com.example.smarteducationsystem_back.mapper.ProfileMapper;
import com.example.smarteducationsystem_back.mapper.SysUserMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import com.example.smarteducationsystem_back.security.CurrentUser;
import com.example.smarteducationsystem_back.security.JwtUtils;
import com.example.smarteducationsystem_back.mapper.SysMetricConfigMapper;
import com.example.smarteducationsystem_back.entity.SysMetricConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "认证与权限接口")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SysMetricConfigMapper configMapper;

    @PostMapping("/login")
    @Operation(summary = "登录获取Token")
    public Result<AuthDto.LoginResp> login(@RequestBody AuthDto.LoginReq req) {
        SysUser user = sysUserMapper.findByUsername(req.getUsername());
        if (user == null || !user.getPassword().equals(req.getPassword())) {
            return Result.error(400, "用户名或密码错误");
        }

        // 维护模式拦截
        SysMetricConfig config = configMapper.getConfig();
        if (config != null && Boolean.TRUE.equals(config.getMaintenanceMode())) {
            String role = user.getRoleType();
            if (!"SCHOOL_ADMIN".equals(role)) {
                return Result.error(503, "系统正在维护中，暂时无法登录。请联系管理员！");
            }
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleType());
        AuthDto.LoginResp resp = new AuthDto.LoginResp();
        resp.setToken(token);
        return Result.success(resp);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息与权限")
    @CheckRole // 任何登录用户均可访问
    public Result<AuthDto.MeResp> me() {
        Integer userId = CurrentUser.getUserId();
        SysUser user = sysUserMapper.findById(userId);

        AuthDto.MeResp resp = new AuthDto.MeResp();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setRoles(Arrays.asList(user.getRoleType()));
        
        // 模拟给些全量权限
        resp.setPermissions(Arrays.asList("dashboard:view", "analysis:compare", "prediction:view", "admin:metrics:edit"));

        AuthDto.DataScope scope = new AuthDto.DataScope();
        scope.setCourseIds(new ArrayList<>());
        scope.setClassIds(new ArrayList<>());

        // 根据角色设置 DataScope，依照 Guide.txt 要求
        switch (user.getRoleType()) {
            case "SYS_ADMIN":
            case "SCHOOL_ADMIN":
                scope.setType("ALL");
                break;
            case "COLLEGE_ADMIN":
                scope.setType("COLLEGE");
                scope.setCollegeId(user.getCollegeId());
                break;
            case "TEACHER":
                scope.setType("TEACHING");
                scope.setCollegeId(user.getCollegeId());
                // 这里本应当继续查出负责的课程与班级，目前由于轻量实现暂置空，由前端先不做强过滤
                break;
            case "STUDENT":
                scope.setType("SELF");
                scope.setStudentId(user.getStudentId());
                // 从 dim_student 查出 collegeId/majorId/gradeId/classId
                if (user.getStudentId() != null) {
                    ProfileDto.StudentProfile sp = profileMapper.getStudentProfile(user.getStudentId());
                    if (sp != null) {
                        scope.setCollegeId(sp.getCollegeId());
                        scope.setMajorId(sp.getMajorId());
                        scope.setGradeId(sp.getGradeId());
                        scope.setClassId(sp.getClassId());
                    }
                }
                break;
            default:
                scope.setType("ALL");
        }
        resp.setDataScope(scope);

        return Result.success(resp);
    }
}
