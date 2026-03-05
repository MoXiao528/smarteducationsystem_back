package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.ForecastUtils;
import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.dto.PredictDto;
import com.example.smarteducationsystem_back.entity.SysMetricConfig;
import com.example.smarteducationsystem_back.entity.SysUser;
import com.example.smarteducationsystem_back.mapper.PredictMapper;
import com.example.smarteducationsystem_back.mapper.SysMetricConfigMapper;
import com.example.smarteducationsystem_back.mapper.SysUserMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import com.example.smarteducationsystem_back.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predict")
@Tag(name = "Predict", description = "智能预测模型接口")
@CheckRole({"TEACHER", "COLLEGE_ADMIN", "SCHOOL_ADMIN", "SYS_ADMIN"})
public class PredictController {

    @Autowired
    private PredictMapper predictMapper;

    @Autowired
    private SysMetricConfigMapper configMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    private double round(double value) {
        return new BigDecimal(value).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    @GetMapping("/subject-trend")
    @Operation(summary = "学科趋势预测")
    public Result<PredictDto.SubjectTrendResp> subjectTrend(PredictDto.SubjectTrendReq req) {

        // ---- 参数默认值 ----
        if (req.getDimension() == null) req.setDimension("COLLEGE");
        if (req.getHorizon() == null || req.getHorizon() < 1) req.setHorizon(1);
        if (req.getHorizon() > 3) req.setHorizon(3);
        if (req.getModel() == null) req.setModel("MA");
        if (req.getTimeGranularity() == null) req.setTimeGranularity("SEMESTER");

        // ---- DataScope 注入（教师/院管理员只能看自己学院） ----
        String roleType = CurrentUser.getRoleType();
        if (("COLLEGE_ADMIN".equals(roleType) || "TEACHER".equals(roleType))
                && "COLLEGE".equals(req.getDimension())) {
            SysUser user = sysUserMapper.findById(CurrentUser.getUserId());
            if (user != null && user.getCollegeId() != null) {
                req.setDimensionId(user.getCollegeId());
            }
        }

        // ---- 读取指标口径配置 ----
        SysMetricConfig config = configMapper.getConfig();
        Double pass = config != null ? config.getPassScore() : 60.0;
        Double exc = config != null ? config.getExcellentScore() : 85.0;

        // ---- 查询历史数据 ----
        List<Map<String, Object>> rawHistory;
        boolean isSemester = "SEMESTER".equals(req.getTimeGranularity());

        if (isSemester) {
            rawHistory = predictMapper.calcDimensionTrendBySemester(
                    req.getDimension(), req.getDimensionId(),
                    req.getStartSemesterId(), req.getEndSemesterId(),
                    pass, exc);
        } else {
            rawHistory = predictMapper.calcDimensionTrendByYear(
                    req.getDimension(), req.getDimensionId(),
                    req.getStartSemesterId(), req.getEndSemesterId(),
                    pass, exc);
        }

        // ---- 构建历史点 ----
        List<PredictDto.TrendPoint> history = new ArrayList<>();
        for (Map<String, Object> row : rawHistory) {
            PredictDto.TrendPoint pt = new PredictDto.TrendPoint();
            pt.setLabel(isSemester ? (String) row.get("semesterName") : (String) row.get("yearLabel"));
            pt.setAvgScore(round(((Number) row.get("avgScore")).doubleValue()));
            pt.setPassRate(round(((Number) row.get("passRate")).doubleValue()));
            pt.setExcellentRate(round(((Number) row.get("excellentRate")).doubleValue()));
            pt.setEnrollCount(((Number) row.get("enrollCount")).intValue());
            history.add(pt);
        }

        // ---- 预测 ----
        List<Double> avgScores = history.stream().map(PredictDto.TrendPoint::getAvgScore).collect(Collectors.toList());
        List<Double> passRates = history.stream().map(PredictDto.TrendPoint::getPassRate).collect(Collectors.toList());
        List<Double> excRates = history.stream().map(PredictDto.TrendPoint::getExcellentRate).collect(Collectors.toList());
        List<Double> enrollCounts = history.stream().map(p -> (double) p.getEnrollCount()).collect(Collectors.toList());

        int horizon = req.getHorizon();
        String model = req.getModel();

        double[] predAvg, predPass, predExc, predEnroll;
        if ("LR".equals(model)) {
            predAvg = ForecastUtils.linearRegression(avgScores, horizon);
            predPass = ForecastUtils.linearRegression(passRates, horizon);
            predExc = ForecastUtils.linearRegression(excRates, horizon);
            predEnroll = ForecastUtils.linearRegression(enrollCounts, horizon);
        } else {
            int ws = Math.min(3, avgScores.size());
            predAvg = ForecastUtils.movingAverage(avgScores, horizon, ws);
            predPass = ForecastUtils.movingAverage(passRates, horizon, ws);
            predExc = ForecastUtils.movingAverage(excRates, horizon, ws);
            predEnroll = ForecastUtils.movingAverage(enrollCounts, horizon, ws);
        }

        // 计算 MAE（基于 avgScore）
        double mae = ForecastUtils.calcMAE(avgScores, model);

        // ---- 生成未来学期标签 ----
        List<String> futureLabels = generateFutureLabels(history, horizon, isSemester);

        // ---- 构建预测点 ----
        List<PredictDto.ForecastPoint> forecast = new ArrayList<>();
        for (int i = 0; i < horizon; i++) {
            PredictDto.ForecastPoint fp = new PredictDto.ForecastPoint();
            fp.setLabel(i < futureLabels.size() ? futureLabels.get(i) : "Future-" + (i + 1));
            fp.setAvgScore(round(predAvg[i]));
            fp.setPassRate(round(Math.max(0, Math.min(1, predPass[i]))));
            fp.setExcellentRate(round(Math.max(0, Math.min(1, predExc[i]))));
            fp.setEnrollCount((int) Math.max(0, Math.round(predEnroll[i])));

            PredictDto.Confidence conf = new PredictDto.Confidence();
            double[] ci = ForecastUtils.confidenceInterval(predAvg[i], mae, i + 1);
            conf.setLower(round(ci[0]));
            conf.setUpper(round(ci[1]));
            fp.setConfidence(conf);

            forecast.add(fp);
        }

        // ---- 指标汇总 ----
        PredictDto.PredictMetrics metrics = new PredictDto.PredictMetrics();
        metrics.setMae(round(mae));
        if (avgScores.size() >= 2) {
            double last = avgScores.get(avgScores.size() - 1);
            double prev = avgScores.get(avgScores.size() - 2);
            double change = (last - prev) / prev;
            metrics.setChangeRate(round(change));
            metrics.setTrend(change > 0.01 ? "UP" : (change < -0.01 ? "DOWN" : "STABLE"));
        } else {
            metrics.setChangeRate(0.0);
            metrics.setTrend("STABLE");
        }

        // ---- 生成建议文本 ----
        String suggestion = generateSuggestion(req, metrics, forecast, history);

        // ---- 组装响应 ----
        PredictDto.SubjectTrendResp resp = new PredictDto.SubjectTrendResp();
        resp.setHistory(history);
        resp.setForecast(forecast);
        resp.setMetrics(metrics);
        resp.setSuggestion(suggestion);

        return Result.success(resp);
    }

    /**
     * 生成未来学期/学年标签
     */
    private List<String> generateFutureLabels(List<PredictDto.TrendPoint> history, int horizon, boolean isSemester) {
        List<String> labels = new ArrayList<>();
        if (history.isEmpty()) {
            for (int i = 0; i < horizon; i++) labels.add("Future-" + (i + 1));
            return labels;
        }

        String lastLabel = history.get(history.size() - 1).getLabel();

        if (isSemester) {
            // 格式: "2024-Fall" -> 下一个: "2025-Spring" -> "2025-Fall" ...
            String[] parts = lastLabel.split("-");
            int year = Integer.parseInt(parts[0]);
            boolean isFall = "Fall".equals(parts[1]);

            for (int i = 0; i < horizon; i++) {
                if (isFall) {
                    year++;
                    isFall = false;
                    labels.add(year + "-Spring");
                } else {
                    isFall = true;
                    labels.add(year + "-Fall");
                }
            }
        } else {
            // 格式: "2024" -> "2025" -> ...
            int year = Integer.parseInt(lastLabel);
            for (int i = 0; i < horizon; i++) {
                labels.add(String.valueOf(year + i + 1));
            }
        }
        return labels;
    }

    /**
     * 生成 AI 建议文本
     */
    private String generateSuggestion(PredictDto.SubjectTrendReq req, PredictDto.PredictMetrics metrics,
                                      List<PredictDto.ForecastPoint> forecast, List<PredictDto.TrendPoint> history) {
        StringBuilder sb = new StringBuilder();
        String dimName = "COLLEGE".equals(req.getDimension()) ? "该学院" :
                          "MAJOR".equals(req.getDimension()) ? "该专业" : "该课程";

        // 趋势描述
        switch (metrics.getTrend()) {
            case "UP":
                sb.append("📈 ").append(dimName).append("平均分呈上升趋势");
                break;
            case "DOWN":
                sb.append("📉 ").append(dimName).append("平均分呈下降趋势");
                break;
            default:
                sb.append("📊 ").append(dimName).append("平均分保持稳定");
        }

        sb.append("，变化幅度 ").append(String.format("%.1f%%", metrics.getChangeRate() * 100)).append("。");

        // 预测值
        if (!forecast.isEmpty()) {
            PredictDto.ForecastPoint nextFp = forecast.get(0);
            sb.append("预测下一期平均分为 ").append(String.format("%.1f", nextFp.getAvgScore()));
            sb.append("（置信区间 ").append(String.format("%.1f", nextFp.getConfidence().getLower()));
            sb.append(" ~ ").append(String.format("%.1f", nextFp.getConfidence().getUpper())).append("）。");
        }

        // 及格率预警
        if (!history.isEmpty()) {
            PredictDto.TrendPoint last = history.get(history.size() - 1);
            if (last.getPassRate() < 0.80) {
                sb.append("⚠️ 当前及格率仅 ").append(String.format("%.1f%%", last.getPassRate() * 100));
                sb.append("，建议关注教学质量并采取补救措施。");
            }
        }

        return sb.toString();
    }
}
