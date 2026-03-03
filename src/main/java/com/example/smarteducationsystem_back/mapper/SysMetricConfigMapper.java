package com.example.smarteducationsystem_back.mapper;

import com.example.smarteducationsystem_back.entity.SysMetricConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysMetricConfigMapper {
    @Select("SELECT * FROM sys_metric_config LIMIT 1")
    SysMetricConfig getConfig();

    @Update("UPDATE sys_metric_config SET pass_score=#{passScore}, excellent_score=#{excellentScore}, score_bins=#{scoreBins} WHERE id=#{id}")
    int updateConfig(SysMetricConfig config);
}
