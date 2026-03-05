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
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate, " +
            "  COUNT(DISTINCT s.course_id) as courseCount, " +
            "  IFNULL(SUM(c.credit), 0) as totalCredit, " +
            "  SUM(CASE WHEN s.score &lt; #{pass} THEN 1 ELSE 0 END) as failCount " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "JOIN dim_course c ON s.course_id = c.id " +
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

    // ===== 教师群体对比 =====

    @Select("SELECT " +
            "  c.id as courseId, c.name as courseName, " +
            "  COUNT(*) as studentCount, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_course c ON s.course_id = c.id " +
            "WHERE s.teacher_id = #{teacherId} AND s.is_absent = 0 " +
            "GROUP BY c.id, c.name " +
            "ORDER BY avgScore DESC")
    List<Map<String, Object>> calcTeacherCourseMetrics(
            @Param("teacherId") Integer teacherId,
            @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("SELECT " +
            "  cl.id as classId, cl.name as className, " +
            "  COUNT(*) as studentCount, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_class cl ON s.class_id = cl.id " +
            "WHERE s.teacher_id = #{teacherId} AND s.is_absent = 0 " +
            "GROUP BY cl.id, cl.name " +
            "ORDER BY avgScore DESC")
    List<Map<String, Object>> calcTeacherClassMetrics(
            @Param("teacherId") Integer teacherId,
            @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("SELECT " +
            "  sm.name as semesterName, " +
            "  c.id as courseId, c.name as courseName, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "JOIN dim_course c ON s.course_id = c.id " +
            "WHERE s.teacher_id = #{teacherId} AND s.is_absent = 0 " +
            "GROUP BY sm.id, sm.name, sm.start_date, c.id, c.name " +
            "ORDER BY sm.start_date ASC, c.id")
    List<Map<String, Object>> calcTeacherCourseTrend(
            @Param("teacherId") Integer teacherId,
            @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  IFNULL(SUM(CASE WHEN score IS NULL THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 0) as scoreNullRate, " +
            "  IFNULL(SUM(CASE WHEN is_absent = 1 THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 0) as absentRate, " +
            "  (SELECT COUNT(*) FROM (SELECT student_id, course_id FROM fact_score " +
            "   <where> <if test='semesterId != null'> semester_id = #{semesterId} </if> </where> " +
            "   GROUP BY student_id, course_id HAVING COUNT(*) &gt; 1) t) as duplicateRecordCount " +
            "FROM fact_score " +
            "<where>" +
            "   <if test='semesterId != null'> AND semester_id = #{semesterId} </if>" +
            "</where>" +
            "</script>")
    OlapDto.DataQuality calcDataQuality(@Param("semesterId") Integer semesterId);

    @Select("<script>" +
            "SELECT " +
            "  <choose>" +
            "    <when test='by == \"college\"'> c.id, c.name </when>" +
            "    <when test='by == \"major\"'> m.id, m.name </when>" +
            "    <otherwise> c.id, c.name </otherwise>" +
            "  </choose>" +
            "  ," +
            "  <choose>" +
            "    <when test='metric == \"avgScore\"'> IFNULL(AVG(s.score), 0) as value </when>" +
            "    <when test='metric == \"passRate\"'> IFNULL(SUM(CASE WHEN s.score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as value </when>" +
            "    <when test='metric == \"excellentRate\"'> IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as value </when>" +
            "    <otherwise> IFNULL(AVG(s.score), 0) as value </otherwise>" +
            "  </choose>" +
            "FROM fact_score s " +
            "  <choose>" +
            "    <when test='by == \"college\"'> JOIN dim_college c ON s.college_id = c.id </when>" +
            "    <when test='by == \"major\"'> JOIN dim_major m ON s.major_id = m.id </when>" +
            "    <otherwise> JOIN dim_college c ON s.college_id = c.id </otherwise>" +
            "  </choose>" +
            "<where>" +
            "   s.is_absent = 0 " +
            "   <if test='semesterId != null'> AND s.semester_id = #{semesterId} </if>" +
            "   <if test='gradeId != null'> AND s.grade_id = #{gradeId} </if>" +
            "   <if test='collegeId != null and by == \"major\"'> AND s.college_id = #{collegeId} </if>" +
            "</where>" +
            "GROUP BY " +
            "  <choose>" +
            "    <when test='by == \"college\"'> c.id, c.name </when>" +
            "    <when test='by == \"major\"'> m.id, m.name </when>" +
            "    <otherwise> c.id, c.name </otherwise>" +
            "  </choose> " +
            "ORDER BY value DESC" +
            "</script>")
    List<OlapDto.RankingItem> calcRankings(@Param("semesterId") Integer semesterId, @Param("gradeId") Integer gradeId, @Param("collegeId") Integer collegeId, @Param("by") String by, @Param("metric") String metric, @Param("pass") Double pass, @Param("exc") Double exc);
}
