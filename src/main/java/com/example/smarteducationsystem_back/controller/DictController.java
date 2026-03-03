package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.entity.*;
import com.example.smarteducationsystem_back.mapper.DictMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@Tag(name = "Dict", description = "字典与多维基础数据接口")
@CheckRole // 登录即可访问
public class DictController {

    @Autowired
    private DictMapper dictMapper;

    @GetMapping("/semesters")
    @Operation(summary = "获取所有学期列表")
    public Result<List<DimSemester>> getSemesters() {
        return Result.success(dictMapper.findAllSemesters());
    }

    @GetMapping("/colleges")
    @Operation(summary = "获取所有学院列表")
    public Result<List<DimCollege>> getColleges() {
        return Result.success(dictMapper.findAllColleges());
    }

    @GetMapping("/majors")
    @Operation(summary = "获取专业列表 (支持学院过滤)")
    public Result<List<DimMajor>> getMajors(@RequestParam(required = false) Integer collegeId) {
        return Result.success(dictMapper.findMajors(collegeId));
    }

    @GetMapping("/grades")
    @Operation(summary = "获取年级列表")
    public Result<List<DimGrade>> getGrades() {
        return Result.success(dictMapper.findAllGrades());
    }

    @GetMapping("/classes")
    @Operation(summary = "获取班级列表 (支持年级和专业过滤)")
    public Result<List<DimClass>> getClasses(@RequestParam(required = false) Integer gradeId, 
                                             @RequestParam(required = false) Integer majorId) {
        return Result.success(dictMapper.findClasses(gradeId, majorId));
    }

    @GetMapping("/courses")
    @Operation(summary = "获取课程列表 (支持学期、学院过滤)")
    public Result<List<DimCourse>> getCourses(@RequestParam(required = false) Integer semesterId,
                                              @RequestParam(required = false) Integer collegeId,
                                              @RequestParam(required = false) Integer majorId) {
        return Result.success(dictMapper.findCourses(semesterId, collegeId, majorId));
    }
}
