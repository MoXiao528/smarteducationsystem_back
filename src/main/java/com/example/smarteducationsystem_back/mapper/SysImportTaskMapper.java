package com.example.smarteducationsystem_back.mapper;

import com.example.smarteducationsystem_back.entity.SysImportTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysImportTaskMapper {

    @Insert("INSERT INTO sys_import_task(type, semester_id, status, total_rows, success_rows, failed_rows, created_at) " +
            "VALUES(#{type}, #{semesterId}, #{status}, #{totalRows}, #{successRows}, #{failedRows}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    int insert(SysImportTask task);

    @Select("SELECT * FROM sys_import_task ORDER BY task_id DESC")
    List<SysImportTask> findAll();

    @Select("SELECT * FROM sys_import_task WHERE task_id = #{taskId}")
    SysImportTask findById(Long taskId);
}
