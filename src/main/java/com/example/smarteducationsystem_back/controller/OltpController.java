package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.entity.SysUser;
import com.example.smarteducationsystem_back.mapper.SysUserMapper;
import com.example.smarteducationsystem_back.security.CurrentUser;
import com.example.smarteducationsystem_back.common.PageResult;
import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.dto.ScoreDto;
import com.example.smarteducationsystem_back.mapper.FactScoreMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/oltp")
@Tag(name = "OLTP", description = "明细数据查询接口")
@CheckRole
public class OltpController {

    @Autowired
    private FactScoreMapper factScoreMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @GetMapping("/scores")
    @Operation(summary = "获取成绩明细列表(分页)")
    public Result<PageResult<ScoreDto.Item>> getScores(ScoreDto.Req req) {
        
        // DataScope: 强制隔离，防止越权
        if ("STUDENT".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getStudentId() != null) {
                req.setStudentId(user.getStudentId());
            } else {
                return Result.error(403, "没有学生关联信息，禁止查询");
            }
        } else if ("TEACHER".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getTeacherId() != null) {
                req.setTeacherId(user.getTeacherId());
            } else {
                return Result.error(403, "没有教师关联信息，禁止查询");
            }
        } else if ("COLLEGE_ADMIN".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getCollegeId() != null) {
                req.setCollegeId(user.getCollegeId());
            } else {
                return Result.error(403, "缺少院系关联信息，禁止查询");
            }
        }

        // 解析 sort: "score,desc" 转化为 "score desc"
        String orderBy = null;
        if (StringUtils.hasText(req.getSort())) {
            String[] parts = req.getSort().split(",");
            if (parts.length == 2) {
                orderBy = parts[0] + " " + parts[1];
            } else {
                orderBy = req.getSort();
            }
        }

        PageHelper.startPage(req.getPage(), req.getSize(), orderBy);
        List<ScoreDto.Item> list = factScoreMapper.selectScores(req);
        Page<ScoreDto.Item> pageInfo = (Page<ScoreDto.Item>) list;

        PageResult<ScoreDto.Item> result = new PageResult<>(
                list,
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal()
        );

        return Result.success(result);
    }

    @PostMapping("/scores/update")
    @Operation(summary = "更新成绩明细")
    @CheckRole({"TEACHER", "SYS_ADMIN", "SCHOOL_ADMIN"})
    public Result<Void> updateScore(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Long id = ((Number) body.get("id")).longValue();
        Double score = body.get("score") != null ? ((Number) body.get("score")).doubleValue() : null;
        Integer isAbsent = body.get("isAbsent") != null ? ((Number) body.get("isAbsent")).intValue() : 0;

        com.example.smarteducationsystem_back.entity.FactScore factScore = factScoreMapper.findById(id);
        if (factScore == null) {
            return Result.error(404, "成绩记录不存在");
        }

        // DataScope for Teacher: Can only edit their own courses
        if ("TEACHER".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user == null || user.getTeacherId() == null || !user.getTeacherId().equals(factScore.getTeacherId())) {
                return Result.error(403, "越权操作：只能修改自己任教课程的成绩");
            }
        }

        int updated = factScoreMapper.updateScore(id, score, isAbsent);
        if (updated > 0) {
            return Result.success();
        } else {
            return Result.error(500, "更新失败");
        }
    }

    @Autowired
    private com.example.smarteducationsystem_back.mapper.FactEnrollMapper factEnrollMapper;

    @GetMapping("/enrolls")
    @Operation(summary = "获取选课记录列表(分页)")
    public Result<PageResult<Map<String, Object>>> getEnrolls(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer courseId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Integer page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") Integer size) {

        Integer filterCollegeId = null;
        Integer filterTeacherId = null;
        Integer filterStudentId = null;

        if ("STUDENT".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            filterStudentId = user.getStudentId();
        } else if ("TEACHER".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            filterTeacherId = user.getTeacherId();
        } else if ("COLLEGE_ADMIN".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            filterCollegeId = user.getCollegeId();
        }

        PageHelper.startPage(page, size);
        List<Map<String, Object>> list = factEnrollMapper.selectEnrolls(filterCollegeId, filterTeacherId, filterStudentId, courseId);
        Page<Map<String, Object>> pageInfo = (Page<Map<String, Object>>) list;

        return Result.success(new PageResult<>(list, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal()));
    }

    @GetMapping("/courses")
    @Operation(summary = "获取课程列表及选课人数统计(分页)")
    public Result<PageResult<Map<String, Object>>> getCourses(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Integer page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") Integer size) {

        Integer filterCollegeId = null;
        Integer filterTeacherId = null;

        if ("TEACHER".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            filterTeacherId = user.getTeacherId();
        } else if ("COLLEGE_ADMIN".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            filterCollegeId = user.getCollegeId();
        }

        PageHelper.startPage(page, size);
        List<Map<String, Object>> list = factEnrollMapper.selectCoursesWithEnrollCount(filterCollegeId, filterTeacherId);
        Page<Map<String, Object>> pageInfo = (Page<Map<String, Object>>) list;

        return Result.success(new PageResult<>(list, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal()));
    }
}
