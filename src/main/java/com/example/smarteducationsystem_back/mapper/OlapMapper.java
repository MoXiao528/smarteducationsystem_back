package com.example.smarteducationsystem_back.mapper;

import com.example.smarteducationsystem_back.dto.OlapDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OlapMapper {

    @Select("<script>" +
            "SELECT COUNT(DISTINCT student_id) FROM fact_score " +
            "<where>" +
            "   <if test='req.semesterId != null'> AND semester_id = #{req.semesterId}</if>" +
            "   <if test='req.collegeId != null'> AND college_id = #{req.collegeId}</if>" +
            "   <if test='req.gradeId != null'> AND grade_id = #{req.gradeId}</if>" +
            "</where>" +
            "</script>")
    Integer countStudents(@Param("req") OlapDto.Req req);
    
    @Select("<script>" +
            "SELECT COUNT(DISTINCT teacher_id) FROM fact_score " +
            "<where>" +
            "   <if test='req.semesterId != null'> AND semester_id = #{req.semesterId}</if>" +
            "   <if test='req.collegeId != null'> AND college_id = #{req.collegeId}</if>" +
            "</where>" +
            "</script>")
    Integer countTeachers(@Param("req") OlapDto.Req req);

    @Select("<script>" +
            "SELECT COUNT(DISTINCT course_id) FROM fact_score " +
            "<where>" +
            "   <if test='req.semesterId != null'> AND semester_id = #{req.semesterId}</if>" +
            "   <if test='req.collegeId != null'> AND college_id = #{req.collegeId}</if>" +
            "</where>" +
            "</script>")
    Integer countCourses(@Param("req") OlapDto.Req req);

    @Select("<script>" +
            "SELECT COUNT(*) FROM fact_score " +
            "<where>" +
            "   <if test='req.semesterId != null'> AND semester_id = #{req.semesterId}</if>" +
            "   <if test='req.collegeId != null'> AND college_id = #{req.collegeId}</if>" +
            "   <if test='req.gradeId != null'> AND grade_id = #{req.gradeId}</if>" +
            "</where>" +
            "</script>")
    Integer countEnrolls(@Param("req") OlapDto.Req req);

    // Using dummy threshold passed from config. Note: in real scenarios we might query it directly in XML or Service.
    @Select("<script>" +
            "SELECT " +
            " IFNULL(AVG(score), 0) as avgScore, " +
            " IFNULL(SUM(CASE WHEN score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(score), 0), 0) as passRate, " +
            " IFNULL(SUM(CASE WHEN score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(score), 0), 0) as excellentRate " +
            "FROM fact_score " +
            "<where>" +
            "   is_absent = 0 " +
            "   <if test='req.semesterId != null'> AND semester_id = #{req.semesterId}</if>" +
            "   <if test='req.collegeId != null'> AND college_id = #{req.collegeId}</if>" +
            "   <if test='req.gradeId != null'> AND grade_id = #{req.gradeId}</if>" +
            "</where>" +
            "</script>")
    Map<String, Object> calcMetrics(@Param("req") OlapDto.Req req, @Param("pass") Double pass, @Param("exc") Double exc);
    
    @Select("<script>" +
            "SELECT " +
            "  sm.name as semesterName, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "<where>" +
            "   s.is_absent = 0 " +
            "   <if test='collegeId != null'> AND s.college_id = #{collegeId}</if>" +
            "</where>" +
            "GROUP BY s.semester_id, sm.name, sm.start_date " +
            "ORDER BY sm.start_date ASC " +
            "</script>")
    List<Map<String, Object>> calcTrend(@Param("collegeId") Integer collegeId, @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  sm.name as semesterName, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "WHERE s.student_id = #{studentId} AND s.is_absent = 0 " +
            "GROUP BY s.semester_id, sm.name, sm.start_date " +
            "ORDER BY sm.start_date ASC " +
            "</script>")
    List<Map<String, Object>> calcStudentTrend(@Param("studentId") Integer studentId, @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  sm.name as semesterName, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "WHERE s.is_absent = 0 " +
            "  <if test='dimension == \"CLASS\"'> AND s.class_id = #{id} </if>" +
            "  <if test='dimension == \"GRADE\"'> AND s.grade_id = #{id} </if>" +
            "GROUP BY s.semester_id, sm.name, sm.start_date " +
            "ORDER BY sm.start_date ASC " +
            "</script>")
    List<Map<String, Object>> calcGroupTrend(@Param("dimension") String dimension, @Param("id") Integer id, @Param("pass") Double pass, @Param("exc") Double exc);
}
