package com.example.smarteducationsystem_back.controller;

import com.example.smarteducationsystem_back.common.PageResult;
import com.example.smarteducationsystem_back.common.Result;
import com.example.smarteducationsystem_back.dto.AdminDto;
import com.example.smarteducationsystem_back.entity.SysImportTask;
import com.example.smarteducationsystem_back.entity.SysMetricConfig;
import com.example.smarteducationsystem_back.mapper.SysImportTaskMapper;
import com.example.smarteducationsystem_back.mapper.SysMetricConfigMapper;
import com.example.smarteducationsystem_back.security.CheckRole;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Admin", description = "系统配置与管理接口")
@CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN"})
public class AdminController {

    @Autowired
    private SysMetricConfigMapper configMapper;

    @Autowired
    private SysImportTaskMapper importTaskMapper;

    @GetMapping("/config/metric-thresholds")
    @Operation(summary = "获取指标统计的阈值配置 (口径)")
    @CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN"})
    public Result<SysMetricConfig> getMetricConfig() {
        return Result.success(configMapper.getConfig());
    }

    @PutMapping("/config/metric-thresholds")
    @Operation(summary = "更新指标统计阈值 (口径)")
    @CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN"})
    public Result<Void> updateMetricConfig(@RequestBody SysMetricConfig config) {
        SysMetricConfig existing = configMapper.getConfig();
        if (existing == null) {
            return Result.error(500, "系统尚未初始化配置文件");
        }
        config.setId(existing.getId());
        configMapper.updateConfig(config);
        return Result.success();
    }

    @PostMapping("/admin/import")
    @Operation(summary = "上传数据文件进行批量导入分析")
    @CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN"})
    public Result<Map<String, Long>> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam(value = "semesterId", required = false) Integer semesterId) {
        
        SysImportTask task = new SysImportTask();
        task.setType(type);
        task.setSemesterId(semesterId);
        task.setStatus("SUCCESS");
        // Mock processing result
        task.setTotalRows(150);
        task.setSuccessRows(148);
        task.setFailedRows(2);
        
        importTaskMapper.insert(task);

        Map<String, Long> res = new HashMap<>();
        res.put("taskId", task.getTaskId());
        return Result.success(res);
    }

    @GetMapping("/admin/import/tasks")
    @Operation(summary = "获取导入任务历史列表(分页)")
    @CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN"})
    public Result<PageResult<SysImportTask>> getImportTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        PageHelper.startPage(page, size);
        List<SysImportTask> list = importTaskMapper.findAll();
        Page<SysImportTask> pageInfo = (Page<SysImportTask>) list;

        PageResult<SysImportTask> res = new PageResult<>(
                list, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal()
        );
        return Result.success(res);
    }

    @GetMapping("/admin/import/tasks/{taskId}")
    @Operation(summary = "获取导入任务产生的详细错误明细")
    @CheckRole({"SYS_ADMIN", "SCHOOL_ADMIN"})
    public Result<AdminDto.ImportTaskDetail> getImportTaskDetail(@PathVariable Long taskId) {
        SysImportTask task = importTaskMapper.findById(taskId);
        if (task == null) {
            return Result.error(404, "任务不存在");
        }

        AdminDto.ImportTaskDetail detail = new AdminDto.ImportTaskDetail();
        detail.setTaskId(task.getTaskId());
        detail.setStatus(task.getStatus());
        
        AdminDto.ImportTaskSummary sum = new AdminDto.ImportTaskSummary();
        sum.setTotalRows(task.getTotalRows());
        sum.setSuccessRows(task.getSuccessRows());
        sum.setFailedRows(task.getFailedRows());
        detail.setSummary(sum);

        List<Map<String, Object>> errors = new ArrayList<>();
        if (task.getFailedRows() > 0) {
            Map<String, Object> e1 = new HashMap<>();
            e1.put("rowNum", 12);
            e1.put("reason", "studentNo (S20990001) not found in dimension table");
            e1.put("raw", "score: 85, is_absent: false");
            errors.add(e1);
        }
        detail.setErrors(errors);

        return Result.success(detail);
    }
}
