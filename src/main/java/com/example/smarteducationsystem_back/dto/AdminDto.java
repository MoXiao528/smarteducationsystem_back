package com.example.smarteducationsystem_back.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class AdminDto {

    @Data
    public static class ImportTaskSummary {
        private Integer totalRows;
        private Integer successRows;
        private Integer failedRows;
    }

    @Data
    public static class ImportTaskDetail {
        private Long taskId;
        private String status;
        private ImportTaskSummary summary;
        private List<Map<String, Object>> errors;
    }
}
