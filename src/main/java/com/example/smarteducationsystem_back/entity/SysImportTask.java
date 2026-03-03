package com.example.smarteducationsystem_back.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SysImportTask {
    private Long taskId;
    private String type;
    private Integer semesterId;
    private String status;
    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private Date createdAt;
}
