package com.example.smarteducationsystem_back.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class OlapDto {

    @Data
    public static class Req {
        private Integer semesterId;
        private Integer collegeId;
        private Integer gradeId;
        private Integer majorId;
        private Integer classId;
        private Integer courseId;
    }

    @Data
    public static class OverviewMetrics {
        private Integer studentCount;
        private Integer teacherCount;
        private Integer courseCount;
        private Integer enrollCount;
        private Double avgScore;
        private Double passRate;
        private Double excellentRate;
    }

    @Data
    public static class TrendData {
        private List<String> x;
        private List<Double> y;
        private StudentRankingSummary studentRankings;
    }

    @Data
    public static class RankingItem {
        private Integer id;
        private String name;
        private Double value;
        private Boolean isMyCollege;
    }

    @Data
    public static class DataQuality {
        private Double scoreNullRate;
        private Double absentRate;
        private Integer duplicateRecordCount;
    }

    @Data
    public static class CompareData {
        private List<CompareMetricItem> metrics;
        private CompareDistribution distribution;
        private CompareTrend trend;
        private StudentRankingSummary studentRankings;
    }

    @Data
    public static class CompareMetricItem {
        private Integer id;
        private String name;
        private Double avgScore;
        private Double passRate;
        private Double excellentRate;
        private Double stdDev;
        private Integer count;
    }

    @Data
    public static class CompareDistribution {
        private List<String> bins;
        private List<CompareSeries> series;
    }

    @Data
    public static class CompareTrend {
        private List<String> x;
        private List<CompareSeries> series;
    }

    @Data
    public static class CompareSeries {
        private Integer id;
        private String name;
        private List<Double> values;
    }

    @Data
    public static class StudentTrend {
        private List<String> x;
        private List<Double> avgScore;
        private List<Double> passRate;
        private List<Double> excellentRate;
        private List<Integer> courseCount;
        private List<Double> totalCredit;
        private List<Integer> failCount;
    }

    @Data
    public static class StudentRankingSummary {
        private StudentRankingItem classRanking;
        private StudentRankingItem gradeRanking;
    }

    @Data
    public static class StudentRankingItem {
        private String dimension;
        private Integer rank;
        private Integer total;
        private Double topPercent;
        private Double avgScore;
    }

    @Data
    public static class StudentAvgItem {
        private Integer studentId;
        private Double avgScore;
    }
    @Data
    public static class TeacherOverview {
        /** 按课程聚合 [{courseId, courseName, studentCount, avgScore, passRate, excellentRate}] */
        private List<Map<String, Object>> courseMetrics;
        /** 按班级聚合 [{classId, className, studentCount, avgScore, passRate, excellentRate}] */
        private List<Map<String, Object>> classMetrics;
        /** 按课程学期的趋势，前端自行分组渲染多条线：[{semesterName, courseId, courseName, avgScore, passRate, excellentRate}] */
        private List<Map<String, Object>> courseTrend;
    }
}

