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

    private Integer[] resolveSemesterRange(Integer semesterId, Integer startSemesterId, Integer endSemesterId) {
        Integer start = startSemesterId != null ? startSemesterId : semesterId;
        Integer end = endSemesterId != null ? endSemesterId : semesterId;
        if (start != null && end != null && start > end) {
            Integer tmp = start;
            start = end;
            end = tmp;
        }
        return new Integer[]{start, end};
    }
    private OlapDto.StudentRankingItem buildStudentRankingItem(String dimension,
                                                               List<OlapDto.StudentAvgItem> studentAvgList,
                                                               Integer studentId) {
        OlapDto.StudentRankingItem item = new OlapDto.StudentRankingItem();
        item.setDimension(dimension);

        if (studentId == null || studentAvgList == null || studentAvgList.isEmpty()) {
            item.setTotal(0);
            return item;
        }

        List<OlapDto.StudentAvgItem> sorted = new ArrayList<>(studentAvgList);
        sorted.sort((a, b) -> {
            double aScore = a.getAvgScore() == null ? 0.0 : a.getAvgScore();
            double bScore = b.getAvgScore() == null ? 0.0 : b.getAvgScore();
            int cmp = Double.compare(bScore, aScore);
            if (cmp != 0) {
                return cmp;
            }
            Integer aId = a.getStudentId() == null ? Integer.MAX_VALUE : a.getStudentId();
            Integer bId = b.getStudentId() == null ? Integer.MAX_VALUE : b.getStudentId();
            return Integer.compare(aId, bId);
        });

        item.setTotal(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            OlapDto.StudentAvgItem row = sorted.get(i);
            if (studentId.equals(row.getStudentId())) {
                int rank = i + 1;
                item.setRank(rank);
                item.setAvgScore(round(row.getAvgScore() == null ? 0.0 : row.getAvgScore()));
                item.setTopPercent(round(rank * 100.0 / sorted.size()));
                break;
            }
        }

        return item;
    }

    @GetMapping("/overview/metrics")
    @Operation(summary = "Get overview metrics")
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
    @Operation(summary = "Get overview trends")
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
    @Operation(summary = "Get overview rankings")
    public Result<List<OlapDto.RankingItem>> getOverviewRankings(@RequestParam(required = false) Integer semesterId,
                                                                 @RequestParam(required = false) Integer gradeId,
                                                                 @RequestParam(required = false) Integer collegeId,
                                                                 @RequestParam String by,
                                                                 @RequestParam String metric) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        Integer myCollegeId = null;
        if ("COLLEGE_ADMIN".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getCollegeId() != null) {
                myCollegeId = user.getCollegeId();
                if ("major".equals(by)) {
                    collegeId = myCollegeId; // 强制使用本学院的 collegeId
                }
            }
        }

        List<OlapDto.RankingItem> list = olapMapper.calcRankings(semesterId, gradeId, collegeId, by, metric, pass, exc);

        if ("COLLEGE_ADMIN".equals(CurrentUser.getRoleType()) && "college".equals(by) && myCollegeId != null) {
            for (OlapDto.RankingItem item : list) {
                if (item.getId().equals(myCollegeId)) {
                    item.setIsMyCollege(true);
                }
            }
        }
        return Result.success(list);
    }

    @GetMapping("/overview/data-quality")
    @Operation(summary = "Get data quality metrics")
    public Result<OlapDto.DataQuality> getDataQuality(@RequestParam(required = false) Integer semesterId) {
        OlapDto.DataQuality dq = olapMapper.calcDataQuality(semesterId);
        if (dq == null) {
            dq = new OlapDto.DataQuality();
            dq.setScoreNullRate(0.0);
            dq.setAbsentRate(0.0);
            dq.setDuplicateRecordCount(0);
        }
        return Result.success(dq);
    }

    @GetMapping("/compare")
    @Operation(summary = "Unified compare analysis")
    public Result<OlapDto.CompareData> getCompare(
            @RequestParam String dimension,
            @RequestParam List<Integer> ids,
            @RequestParam(required = false) Integer semesterId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false, defaultValue = "avgScore") String metric) {
        
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        OlapDto.CompareData data = new OlapDto.CompareData();

        if (ids == null || ids.isEmpty()) {
            return Result.success(data);
        }

        // Metrics
        List<OlapDto.CompareMetricItem> metrics = olapMapper.calcCompareMetrics(dimension, ids, semesterId, courseId, pass, exc);
        data.setMetrics(metrics);

        // Distribution
        List<Map<String, Object>> distList = olapMapper.calcCompareDistribution(dimension, ids, semesterId, courseId);
        OlapDto.CompareDistribution dist = new OlapDto.CompareDistribution();
        dist.setBins(Arrays.asList("0-59", "60-69", "70-79", "80-89", "90-100"));
        List<OlapDto.CompareSeries> distSeriesList = new ArrayList<>();
        
        // Match names to ids for series data
        Map<Integer, String> idNameMap = new HashMap<>();
        for (OlapDto.CompareMetricItem item : metrics) {
            idNameMap.put(item.getId(), item.getName());
        }

        for (Map<String, Object> map : distList) {
            Integer id = ((Number) map.get("id")).intValue();
            String name = idNameMap.getOrDefault(id, "未知");
            
            OlapDto.CompareSeries series = new OlapDto.CompareSeries();
            series.setId(id);
            series.setName(name);
            
            long total = ((Number) map.get("total")).longValue();
            if (total == 0) {
                 series.setValues(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0));
            } else {
                 series.setValues(Arrays.asList(
                     round(((Number) map.get("bin1")).doubleValue() / total),
                     round(((Number) map.get("bin2")).doubleValue() / total),
                     round(((Number) map.get("bin3")).doubleValue() / total),
                     round(((Number) map.get("bin4")).doubleValue() / total),
                     round(((Number) map.get("bin5")).doubleValue() / total)
                 ));
            }
            distSeriesList.add(series);
        }
        dist.setSeries(distSeriesList);
        data.setDistribution(dist);

        // Trend
        List<Map<String, Object>> trendList = olapMapper.calcCompareTrend(dimension, ids, courseId, metric, pass, exc);
        OlapDto.CompareTrend trend = new OlapDto.CompareTrend();
        
        List<String> xValues = new ArrayList<>();
        Map<Integer, List<Double>> seriesDataMap = new HashMap<>();
        
        for (Integer id : ids) {
            seriesDataMap.put(id, new ArrayList<>());
        }

        for (Map<String, Object> map : trendList) {
            String xName = (String) map.get("semesterName");
            if (!xValues.contains(xName)) {
                xValues.add(xName);
            }
        }
        
        // Fill data, 0 for missing
        for (String x : xValues) {
             for (Integer id : ids) {
                 boolean found = false;
                 for (Map<String, Object> map : trendList) {
                     if (id.equals(((Number) map.get("id")).intValue()) && x.equals(map.get("semesterName"))) {
                         seriesDataMap.get(id).add(round(((Number) map.get("value")).doubleValue()));
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     seriesDataMap.get(id).add(0.0);
                 }
             }
        }

        trend.setX(xValues);
        List<OlapDto.CompareSeries> trendSeriesList = new ArrayList<>();
        for (Integer id : ids) {
            OlapDto.CompareSeries series = new OlapDto.CompareSeries();
            series.setId(id);
            series.setName(idNameMap.getOrDefault(id, "未知"));
            series.setValues(seriesDataMap.get(id));
            trendSeriesList.add(series);
        }
        trend.setSeries(trendSeriesList);
        data.setTrend(trend);

        if ("STUDENT".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getStudentId() != null) {
                OlapDto.StudentRankingSummary summary = new OlapDto.StudentRankingSummary();
                summary.setClassRanking(buildStudentRankingItem("CLASS",
                        olapMapper.listClassStudentAveragesForStudent(user.getStudentId(), semesterId, semesterId, courseId),
                        user.getStudentId()));
                summary.setGradeRanking(buildStudentRankingItem("GRADE",
                        olapMapper.listGradeStudentAveragesForStudent(user.getStudentId(), semesterId, semesterId, courseId),
                        user.getStudentId()));
                data.setStudentRankings(summary);
            }
        }
        return Result.success(data);
    }

    @GetMapping("/student/{studentId}/trend")
    @Operation(summary = "Get student personal trend")
    public Result<OlapDto.StudentTrend> getStudentTrend(@PathVariable Integer studentId,
                                                        @RequestParam(required = false) Integer semesterId,
                                                        @RequestParam(required = false) Integer startSemesterId,
                                                        @RequestParam(required = false) Integer endSemesterId) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        Integer[] range = resolveSemesterRange(semesterId, startSemesterId, endSemesterId);
        List<Map<String, Object>> list = olapMapper.calcStudentTrend(studentId, range[0], range[1], pass, exc);
        
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
    @Operation(summary = "Get group trend")
    public Result<OlapDto.TrendData> getGroupTrend(@RequestParam String dimension,
                                                   @RequestParam Integer id,
                                                   @RequestParam String metric,
                                                   @RequestParam(required = false) Integer semesterId,
                                                   @RequestParam(required = false) Integer startSemesterId,
                                                   @RequestParam(required = false) Integer endSemesterId) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        Integer[] range = resolveSemesterRange(semesterId, startSemesterId, endSemesterId);
        List<Map<String, Object>> list = olapMapper.calcGroupTrend(dimension, id, range[0], range[1], pass, exc);

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
        // ===== 新增：为学生角色计算班级/年级排名 =====
        if ("STUDENT".equals(CurrentUser.getRoleType())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getStudentId() != null) {
                OlapDto.StudentRankingSummary summary = new OlapDto.StudentRankingSummary();
                summary.setClassRanking(buildStudentRankingItem("CLASS",
                        olapMapper.listClassStudentAveragesForStudent(user.getStudentId(), range[0], range[1], null),
                        user.getStudentId()));
                summary.setGradeRanking(buildStudentRankingItem("GRADE",
                        olapMapper.listGradeStudentAveragesForStudent(user.getStudentId(), range[0], range[1], null),
                        user.getStudentId()));
                trend.setStudentRankings(summary);
            }
        }
        // ===== 新增结束 =====
        return Result.success(trend);
    }

    @Autowired
    private com.example.smarteducationsystem_back.mapper.FactEnrollMapper factEnrollMapper;

    @GetMapping("/enroll/dashboard")
    @Operation(summary = "Get enroll dashboard")
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

    @GetMapping("/teacher/overview")
    @Operation(summary = "Get teacher overview dashboard")
    @CheckRole({"TEACHER", "COLLEGE_ADMIN", "SCHOOL_ADMIN"})
    public Result<OlapDto.TeacherOverview> getTeacherOverview(@RequestParam(required = false) Integer semesterId,
                                                              @RequestParam(required = false) Integer startSemesterId,
                                                              @RequestParam(required = false) Integer endSemesterId,
                                                              @RequestParam(required = false) Integer collegeId,
                                                              @RequestParam(required = false) Integer majorId,
                                                              @RequestParam(required = false) Integer gradeId) {
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
        if (user == null) {
            return Result.error(400, "User not found");
        }

        OlapDto.TeacherOverview overview = new OlapDto.TeacherOverview();
        Integer[] range = resolveSemesterRange(semesterId, startSemesterId, endSemesterId);
        Integer start = range[0];
        Integer end = range[1];

        String roleType = user.getRoleType();
        if ("TEACHER".equals(roleType)) {
            if (user.getTeacherId() == null) {
                return Result.error(400, "Teacher account missing teacherId");
            }
            Integer teacherId = user.getTeacherId();
            overview.setCourseMetrics(olapMapper.calcTeacherCourseMetrics(teacherId, start, end, majorId, gradeId, pass, exc));
            overview.setClassMetrics(olapMapper.calcTeacherClassMetrics(teacherId, start, end, majorId, gradeId, pass, exc));
            overview.setCourseTrend(olapMapper.calcTeacherCourseTrend(teacherId, start, end, majorId, gradeId, pass, exc));
            return Result.success(overview);
        }

        if ("COLLEGE_ADMIN".equals(roleType)) {
            if (user.getCollegeId() == null) {
                return Result.error(400, "College admin account missing collegeId");
            }
            Integer scopedCollegeId = user.getCollegeId();
            overview.setCourseMetrics(olapMapper.calcCollegeCourseMetrics(scopedCollegeId, start, end, majorId, gradeId, pass, exc));
            overview.setClassMetrics(olapMapper.calcCollegeClassMetrics(scopedCollegeId, start, end, majorId, gradeId, pass, exc));
            overview.setCourseTrend(olapMapper.calcCollegeCourseTrend(scopedCollegeId, start, end, majorId, gradeId, pass, exc));
            return Result.success(overview);
        }

        if ("SCHOOL_ADMIN".equals(roleType)) {
            overview.setCourseMetrics(olapMapper.calcCollegeCourseMetrics(collegeId, start, end, majorId, gradeId, pass, exc));
            overview.setClassMetrics(olapMapper.calcCollegeClassMetrics(collegeId, start, end, majorId, gradeId, pass, exc));
            overview.setCourseTrend(olapMapper.calcCollegeCourseTrend(collegeId, start, end, majorId, gradeId, pass, exc));
            return Result.success(overview);
        }

        return Result.error(403, "Current role is not supported by group trend dashboard");
    }
}




