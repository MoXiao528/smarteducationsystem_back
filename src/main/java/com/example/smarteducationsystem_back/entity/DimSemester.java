package com.example.smarteducationsystem_back.entity;

import lombok.Data;
import java.util.Date;

@Data
public class DimSemester {
    private Integer id;
    private String name;
    private Date startDate;
    private Date endDate;
}
