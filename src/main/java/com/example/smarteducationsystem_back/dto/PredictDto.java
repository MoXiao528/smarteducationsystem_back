package com.example.smarteducationsystem_back.dto;

import lombok.Data;
import java.util.List;

public class PredictDto {

    @Data
    public static class SubjectTrendReq {
        /** COLLEGE / MAJOR / COURSE */
        private String dimension;
        /** 对应维度的 ID */
        private Integer dimensionId;
        /** SEMESTER(默认) / YEAR */
        private String timeGranularity = "SEMESTER";
        /** 起始学期 ID（可选） */
        private Integer startSemesterId;
        /** 截止学期 ID（可选） */
        private Integer endSemesterId;
        /** 预测步长，默认 1 */
        private Integer horizon = 1;
        /** 预测模型 MA(移动平均) / LR(线性回归)，默认 MA */
        private String model = "MA";
    }

    @Data
    public static class SubjectTrendResp {
        private List<TrendPoint> history;
        private List<ForecastPoint> forecast;
        private PredictMetrics metrics;
        private String suggestion;
    }

    @Data
    public static class TrendPoint {
        private String label;
        private Double avgScore;
        private Double passRate;
        private Double excellentRate;
        private Integer enrollCount;
    }

    @Data
    public static class ForecastPoint {
        private String label;
        private Double avgScore;
        private Double passRate;
        private Double excellentRate;
        private Integer enrollCount;
        private Confidence confidence;
    }

    @Data
    public static class Confidence {
        private Double lower;
        private Double upper;
    }

    @Data
    public static class PredictMetrics {
        private Double mae;
        /** UP / DOWN / STABLE */
        private String trend;
        /** 变化幅度百分比 */
        private Double changeRate;
    }
}
