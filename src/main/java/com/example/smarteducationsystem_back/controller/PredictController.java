package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.security.CheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predict")
@Tag(name = "Predict", description = "智能预测模型接口")
@CheckRole
public class PredictController {

    @GetMapping("/enroll")
    @Operation(summary = "课程选课容量预测")
    public Result<Map<String, Object>> predictEnroll(
            @RequestParam Integer courseId,
            @RequestParam(defaultValue = "1") Integer horizon,
            @RequestParam(defaultValue = "MA") String model) {

        Map<String, Object> resp = new HashMap<>();
        
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> h1 = new HashMap<>(); h1.put("x", "2023-Fall"); h1.put("y", 120);
        Map<String, Object> h2 = new HashMap<>(); h2.put("x", "2024-Spring"); h2.put("y", 135);
        history.add(h1); history.add(h2);
        resp.put("history", history);

        List<Map<String, Object>> forecast = new ArrayList<>();
        Map<String, Object> f1 = new HashMap<>(); f1.put("x", "2024-Fall"); f1.put("y", 148);
        forecast.add(f1);
        resp.put("forecast", forecast);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("mae", 8.2);
        metrics.put("rmse", 10.5);
        resp.put("metrics", metrics);

        resp.put("suggestionText", "根据预测模型计算，建议下学期扩容15%-20%，或增加1个班次以满足需求且避免选修紧张。");
        
        return Result.success(resp);
    }
}
