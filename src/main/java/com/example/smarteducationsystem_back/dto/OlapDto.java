package com.example.smarteducationsystem_back.dto;

import lombok.Data;
import java.util.List;

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
    }

    @Data
    public static class RankingItem {
        private Integer id;
        private String name;
        private Double value;
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
}
