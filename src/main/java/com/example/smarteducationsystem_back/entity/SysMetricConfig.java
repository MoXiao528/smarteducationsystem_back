package com.example.smarteducationsystem_back.entity;

import lombok.Data;

@Data
public class SysMetricConfig {
    private Integer id;
    private Double passScore;
    private Double excellentScore;
    private String scoreBins;
}
