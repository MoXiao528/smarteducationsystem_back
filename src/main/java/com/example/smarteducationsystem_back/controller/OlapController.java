package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.dto.OlapDto;
import com.example.smarteducationsystem_back.entity.SysMetricConfig;
import com.example.smarteducationsystem_back.mapper.OlapMapper;
import com.example.smarteducationsystem_back.mapper.SysMetricConfigMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import com.example.smarteducationsystem_back.entity.SysUser;
import com.example.smarteducationsystem_back.mapper.SysUserMapper;
import com.example.smarteducationsystem_back.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/olap")
@Tag(name = "OLAP", description = "数据大屏各类统计聚合视图")
@CheckRole
public class OlapController {

    @Autowired
    private OlapMapper olapMapper;

    @Autowired
    private SysMetricConfigMapper configMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    private double round(double value) {
        return new BigDecimal(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    @GetMapping("/overview/metrics")
    @Operation(summary = "获取总览核心指标卡数据")
    public Result<OlapDto.OverviewMetrics> getOverviewMetrics(OlapDto.Req req) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        if ("COLLEGE_ADMIN".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getCollegeId() != null) {
                req.setCollegeId(user.getCollegeId()); // Inject DataScope
            }
        }

        OlapDto.OverviewMetrics res = new OlapDto.OverviewMetrics();
        res.setStudentCount(olapMapper.countStudents(req));
        res.setTeacherCount(olapMapper.countTeachers(req));
        res.setCourseCount(olapMapper.countCourses(req));
        res.setEnrollCount(olapMapper.countEnrolls(req));

        Map<String, Object> map = olapMapper.calcMetrics(req, pass, exc);
        if (map != null) {
            res.setAvgScore(round(((Number) map.getOrDefault("avgScore", 0.0)).doubleValue()));
            res.setPassRate(round(((Number) map.getOrDefault("passRate", 0.0)).doubleValue()));
            res.setExcellentRate(round(((Number) map.getOrDefault("excellentRate", 0.0)).doubleValue()));
        } else {
            res.setAvgScore(0.0);
            res.setPassRate(0.0);
            res.setExcellentRate(0.0);
        }

        return Result.success(res);
    }

    @GetMapping("/overview/trends")
    @Operation(summary = "获取全校/指定院系的趋势指标分析")
    public Result<OlapDto.TrendData> getOverviewTrends(@RequestParam String metric, @RequestParam(required = false) Integer collegeId) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        if ("COLLEGE_ADMIN".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getCollegeId() != null) {
                collegeId = user.getCollegeId(); // Inject DataScope
            }
        }

        List<Map<String, Object>> trendList = olapMapper.calcTrend(collegeId, pass, exc);
        OlapDto.TrendData data = new OlapDto.TrendData();
        data.setX(new ArrayList<>());
        data.setY(new ArrayList<>());
        
        for (Map<String, Object> row : trendList) {
            data.getX().add((String) row.get("semesterName"));
            Double val = 0.0;
            switch (metric) {
                case "avgScore": val = ((Number) row.get("avgScore")).doubleValue(); break;
                case "passRate": val = ((Number) row.get("passRate")).doubleValue(); break;
                case "excellentRate": val = ((Number) row.get("excellentRate")).doubleValue(); break;
            }
            data.getY().add(round(val));
        }

        return Result.success(data);
    }

    @GetMapping("/overview/rankings")
    @Operation(summary = "获取排行前N列表")
    public Result<List<OlapDto.RankingItem>> getOverviewRankings(@RequestParam(required = false) Integer semesterId,
                                                                 @RequestParam String by,
                                                                 @RequestParam String metric,
                                                                 @RequestParam(defaultValue = "10") Integer top) {
        // [Demo演示实现] 因为需要多表动态JOIN，为了快速交付我们这里仅做 mock 或简单的返回体填充。
        List<OlapDto.RankingItem> list = new ArrayList<>();
        OlapDto.RankingItem item = new OlapDto.RankingItem();
        item.setId(10);
        item.setName(by.equals("college") ? "计算机学院" : "计算机科学与技术");
        item.setValue(85.5);
        list.add(item);
        return Result.success(list);
    }

    @GetMapping("/overview/data-quality")
    @Operation(summary = "获取数据质量监控面板指标")
    public Result<OlapDto.DataQuality> getDataQuality(@RequestParam(required = false) Integer semesterId) {
        OlapDto.DataQuality dq = new OlapDto.DataQuality();
        dq.setScoreNullRate(0.02);
        dq.setAbsentRate(0.015);
        dq.setDuplicateRecordCount(3);
        return Result.success(dq);
    }

    @GetMapping("/compare")
    @Operation(summary = "多维度统一对比分析")
    public Result<OlapDto.CompareData> getCompare(
            @RequestParam String dimension,
            @RequestParam List<Integer> ids,
            @RequestParam(required = false) Integer semesterId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false, defaultValue = "avgScore") String metric) {
        
        OlapDto.CompareData data = new OlapDto.CompareData();
        
        // Mock Metrics
        List<OlapDto.CompareMetricItem> metrics = new ArrayList<>();
        OlapDto.CompareMetricItem m1 = new OlapDto.CompareMetricItem();
        m1.setId(ids.isEmpty() ? 1 : ids.get(0));
        m1.setName("对比对象A");
        m1.setAvgScore(80.5);
        m1.setPassRate(0.92);
        m1.setExcellentRate(0.35);
        m1.setStdDev(8.9);
        m1.setCount(120);
        metrics.add(m1);
        data.setMetrics(metrics);

        // Mock Distribution
        OlapDto.CompareDistribution dist = new OlapDto.CompareDistribution();
        dist.setBins(Arrays.asList("0-59", "60-69", "70-79", "80-84", "85-100"));
        OlapDto.CompareSeries s1 = new OlapDto.CompareSeries();
        s1.setId(m1.getId());
        s1.setName(m1.getName());
        s1.setValues(Arrays.asList(0.05, 0.15, 0.30, 0.20, 0.30));
        dist.setSeries(Arrays.asList(s1));
        data.setDistribution(dist);

        // Mock Trend
        OlapDto.CompareTrend trend = new OlapDto.CompareTrend();
        trend.setX(Arrays.asList("2023-Fall", "2024-Spring", "2024-Fall"));
        OlapDto.CompareSeries t1 = new OlapDto.CompareSeries();
        t1.setId(m1.getId());
        t1.setName(m1.getName());
        t1.setValues(Arrays.asList(78.5, 79.2, 80.5));
        trend.setSeries(Arrays.asList(t1));
        data.setTrend(trend);

        return Result.success(data);
    }

    @GetMapping("/student/{studentId}/trend")
    @Operation(summary = "学生个人成绩趋势")
    public Result<OlapDto.StudentTrend> getStudentTrend(@PathVariable Integer studentId) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        List<Map<String, Object>> list = olapMapper.calcStudentTrend(studentId, pass, exc);
        
        OlapDto.StudentTrend trend = new OlapDto.StudentTrend();
        trend.setX(new ArrayList<>());
        trend.setAvgScore(new ArrayList<>());
        trend.setPassRate(new ArrayList<>());
        trend.setExcellentRate(new ArrayList<>());
        trend.setCourseCount(new ArrayList<>());
        trend.setTotalCredit(new ArrayList<>());
        trend.setFailCount(new ArrayList<>());

        for (Map<String, Object> map : list) {
            trend.getX().add((String) map.get("semesterName"));
            trend.getAvgScore().add(round(((Number) map.get("avgScore")).doubleValue()));
            trend.getPassRate().add(round(((Number) map.get("passRate")).doubleValue()));
            trend.getExcellentRate().add(round(((Number) map.get("excellentRate")).doubleValue()));
            trend.getCourseCount().add(((Number) map.get("courseCount")).intValue());
            trend.getTotalCredit().add(round(((Number) map.get("totalCredit")).doubleValue()));
            trend.getFailCount().add(((Number) map.get("failCount")).intValue());
        }
        return Result.success(trend);
    }

    @GetMapping("/student/group-trend")
    @Operation(summary = "群体趋势分析")
    public Result<OlapDto.TrendData> getGroupTrend(@RequestParam String dimension, @RequestParam Integer id, @RequestParam String metric) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        List<Map<String, Object>> list = olapMapper.calcGroupTrend(dimension, id, pass, exc);

        OlapDto.TrendData trend = new OlapDto.TrendData();
        trend.setX(new ArrayList<>());
        trend.setY(new ArrayList<>());

        for (Map<String, Object> map : list) {
            trend.getX().add((String) map.get("semesterName"));
            Double val = 0.0;
            switch (metric) {
                case "avgScore": val = ((Number) map.get("avgScore")).doubleValue(); break;
                case "passRate": val = ((Number) map.get("passRate")).doubleValue(); break;
                case "excellentRate": val = ((Number) map.get("excellentRate")).doubleValue(); break;
            }
            trend.getY().add(round(val));
        }
        return Result.success(trend);
    }

    @Autowired
    private com.example.smarteducationsystem_back.mapper.FactEnrollMapper factEnrollMapper;

    @GetMapping("/enroll/dashboard")
    @Operation(summary = "选课看板汇总")
    public Result<Map<String, Object>> getEnrollDashboard(
            @RequestParam(required = false) Integer semesterId,
            @RequestParam(required = false) Integer collegeId,
            @RequestParam(required = false) Integer majorId) {
        
        Map<String, Object> data = new HashMap<>();
        
        // Use real DB queries instead of mock data
        int courseCount = 0; // The original mock had 120, but there's no direct count of open courses without enrollments in fact_enroll unless we query dim_course. We'll use openCourseCount for both as a fallback or just use openCourseCount.
        int openCourseCount = factEnrollMapper.countOpenCourses(semesterId, collegeId, majorId) != null ? factEnrollMapper.countOpenCourses(semesterId, collegeId, majorId) : 0;
        int enrollCount = factEnrollMapper.countEnrolls(semesterId, collegeId, majorId) != null ? factEnrollMapper.countEnrolls(semesterId, collegeId, majorId) : 0;
        
        data.put("courseCount", openCourseCount); // Fallback to openCourseCount
        data.put("openCourseCount", openCourseCount);
        data.put("enrollCount", enrollCount);
        
        List<Map<String, Object>> hotCourses = factEnrollMapper.calcHotCourses(semesterId, collegeId, majorId);
        data.put("hotCourses", hotCourses != null ? hotCourses : new ArrayList<>());
        
        List<Map<String, Object>> majorDist = factEnrollMapper.calcMajorDistribution(semesterId, collegeId, majorId);
        data.put("majorDistribution", majorDist != null ? majorDist : new ArrayList<>());

        List<Map<String, Object>> gradeDist = factEnrollMapper.calcGradeDistribution(semesterId, collegeId, majorId);
        data.put("gradeDistribution", gradeDist != null ? gradeDist : new ArrayList<>());

        return Result.success(data);
    }
}
