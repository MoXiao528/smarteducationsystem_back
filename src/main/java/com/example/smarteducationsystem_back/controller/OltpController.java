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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
