package com.example.smarteducationsystem_back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PredictMapper {

    /**
     * 按学期粒度聚合指定维度的历史数据。
     * 返回每行: semesterId, semesterName, avgScore, passRate, excellentRate, enrollCount
     */
    @Select("<script>" +
            "SELECT " +
            "  s.semester_id as semesterId, " +
            "  sm.name as semesterName, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate, " +
            "  COUNT(*) as enrollCount " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "WHERE s.is_absent = 0 " +
            "  <if test='dimension == \"COLLEGE\"'> AND s.college_id = #{dimensionId} </if>" +
            "  <if test='dimension == \"MAJOR\"'>   AND s.major_id = #{dimensionId}   </if>" +
            "  <if test='dimension == \"COURSE\"'>  AND s.course_id = #{dimensionId}  </if>" +
            "  <if test='startSemesterId != null'>  AND s.semester_id &gt;= #{startSemesterId} </if>" +
            "  <if test='endSemesterId != null'>    AND s.semester_id &lt;= #{endSemesterId}   </if>" +
            "GROUP BY s.semester_id, sm.name, sm.start_date " +
            "ORDER BY sm.start_date ASC " +
            "</script>")
    List<Map<String, Object>> calcDimensionTrendBySemester(
            @Param("dimension") String dimension,
            @Param("dimensionId") Integer dimensionId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("pass") Double pass,
            @Param("exc") Double exc);

    /**
     * 按学年粒度聚合。
     * dim_semester.name 格式为 "2022-Fall" / "2023-Spring"，
     * 提取 LEFT(name, 4) 作为学年标签进行 GROUP BY。
     */
    @Select("<script>" +
            "SELECT " +
            "  LEFT(sm.name, 4) as yearLabel, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate, " +
            "  COUNT(*) as enrollCount " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "WHERE s.is_absent = 0 " +
            "  <if test='dimension == \"COLLEGE\"'> AND s.college_id = #{dimensionId} </if>" +
            "  <if test='dimension == \"MAJOR\"'>   AND s.major_id = #{dimensionId}   </if>" +
            "  <if test='dimension == \"COURSE\"'>  AND s.course_id = #{dimensionId}  </if>" +
            "  <if test='startSemesterId != null'>  AND s.semester_id &gt;= #{startSemesterId} </if>" +
            "  <if test='endSemesterId != null'>    AND s.semester_id &lt;= #{endSemesterId}   </if>" +
            "GROUP BY LEFT(sm.name, 4) " +
            "ORDER BY LEFT(sm.name, 4) ASC " +
            "</script>")
    List<Map<String, Object>> calcDimensionTrendByYear(
            @Param("dimension") String dimension,
            @Param("dimensionId") Integer dimensionId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("pass") Double pass,
            @Param("exc") Double exc);

    /**
     * 获取所有学期，用于生成未来预测标签。
     */
    @Select("SELECT id, name FROM dim_semester ORDER BY start_date ASC")
    List<Map<String, Object>> listAllSemesters();
}
