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
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id &lt;= #{endSemesterId}) " +
            "GROUP BY s.semester_id, sm.name, sm.start_date " +
            "ORDER BY sm.start_date ASC " +
            "</script>")
    List<Map<String, Object>> calcStudentTrend(@Param("studentId") Integer studentId, @Param("startSemesterId") Integer startSemesterId, @Param("endSemesterId") Integer endSemesterId, @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  sm.name as semesterName, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score &gt;= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "WHERE s.is_absent = 0 " +
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id &lt;= #{endSemesterId}) " +
            "  <if test='dimension == \"CLASS\"'> AND s.class_id = #{id} </if>" +
            "  <if test='dimension == \"GRADE\"'> AND s.grade_id = #{id} </if>" +
            "GROUP BY s.semester_id, sm.name, sm.start_date " +
            "ORDER BY sm.start_date ASC " +
            "</script>")
    List<Map<String, Object>> calcGroupTrend(@Param("dimension") String dimension, @Param("id") Integer id, @Param("startSemesterId") Integer startSemesterId, @Param("endSemesterId") Integer endSemesterId, @Param("pass") Double pass, @Param("exc") Double exc);

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
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id <= #{endSemesterId}) " +
            "  AND (#{majorId} IS NULL OR s.major_id = #{majorId}) " +
            "  AND (#{gradeId} IS NULL OR s.grade_id = #{gradeId}) " +
            "GROUP BY c.id, c.name " +
            "ORDER BY avgScore DESC")
    List<Map<String, Object>> calcTeacherCourseMetrics(
            @Param("teacherId") Integer teacherId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("majorId") Integer majorId,
            @Param("gradeId") Integer gradeId,
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
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id <= #{endSemesterId}) " +
            "  AND (#{majorId} IS NULL OR s.major_id = #{majorId}) " +
            "  AND (#{gradeId} IS NULL OR s.grade_id = #{gradeId}) " +
            "GROUP BY cl.id, cl.name " +
            "ORDER BY avgScore DESC")
    List<Map<String, Object>> calcTeacherClassMetrics(
            @Param("teacherId") Integer teacherId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("majorId") Integer majorId,
            @Param("gradeId") Integer gradeId,
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
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id <= #{endSemesterId}) " +
            "  AND (#{majorId} IS NULL OR s.major_id = #{majorId}) " +
            "  AND (#{gradeId} IS NULL OR s.grade_id = #{gradeId}) " +
            "GROUP BY sm.id, sm.name, sm.start_date, c.id, c.name " +
            "ORDER BY sm.start_date ASC, c.id")
    List<Map<String, Object>> calcTeacherCourseTrend(
            @Param("teacherId") Integer teacherId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("majorId") Integer majorId,
            @Param("gradeId") Integer gradeId,
            @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  c.id as courseId, c.name as courseName, " +
            "  COUNT(*) as studentCount, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_course c ON s.course_id = c.id " +
            "<where>" +
            "  s.is_absent = 0 " +
            "  <if test='collegeId != null'> AND s.college_id = #{collegeId} </if>" +
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id &lt;= #{endSemesterId}) " +
            "  AND (#{majorId} IS NULL OR s.major_id = #{majorId}) " +
            "  AND (#{gradeId} IS NULL OR s.grade_id = #{gradeId}) " +
            "</where>" +
            "GROUP BY c.id, c.name " +
            "ORDER BY avgScore DESC" +
            "</script>")
    List<Map<String, Object>> calcCollegeCourseMetrics(
            @Param("collegeId") Integer collegeId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("majorId") Integer majorId,
            @Param("gradeId") Integer gradeId,
            @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  cl.id as classId, cl.name as className, " +
            "  COUNT(*) as studentCount, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_class cl ON s.class_id = cl.id " +
            "<where>" +
            "  s.is_absent = 0 " +
            "  <if test='collegeId != null'> AND s.college_id = #{collegeId} </if>" +
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id &lt;= #{endSemesterId}) " +
            "  AND (#{majorId} IS NULL OR s.major_id = #{majorId}) " +
            "  AND (#{gradeId} IS NULL OR s.grade_id = #{gradeId}) " +
            "</where>" +
            "GROUP BY cl.id, cl.name " +
            "ORDER BY avgScore DESC" +
            "</script>")
    List<Map<String, Object>> calcCollegeClassMetrics(
            @Param("collegeId") Integer collegeId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("majorId") Integer majorId,
            @Param("gradeId") Integer gradeId,
            @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  sm.name as semesterName, " +
            "  c.id as courseId, c.name as courseName, " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate " +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "JOIN dim_course c ON s.course_id = c.id " +
            "<where>" +
            "  s.is_absent = 0 " +
            "  <if test='collegeId != null'> AND s.college_id = #{collegeId} </if>" +
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id &lt;= #{endSemesterId}) " +
            "  AND (#{majorId} IS NULL OR s.major_id = #{majorId}) " +
            "  AND (#{gradeId} IS NULL OR s.grade_id = #{gradeId}) " +
            "</where>" +
            "GROUP BY sm.id, sm.name, sm.start_date, c.id, c.name " +
            "ORDER BY sm.start_date ASC, c.id" +
            "</script>")
    List<Map<String, Object>> calcCollegeCourseTrend(
            @Param("collegeId") Integer collegeId,
            @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
            @Param("majorId") Integer majorId,
            @Param("gradeId") Integer gradeId,
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

    @Select("<script>" +
            "SELECT " +
            "  <choose>" +
            "    <when test='dimension == \"COLLEGE\"'> c.id, c.name </when>" +
            "    <when test='dimension == \"MAJOR\"'> m.id, m.name </when>" +
            "    <when test='dimension == \"CLASS\"'> cl.id, cl.name </when>" +
            "    <otherwise> c.id, c.name </otherwise>" +
            "  </choose>" +
            "  , " +
            "  IFNULL(AVG(s.score), 0) as avgScore, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as passRate, " +
            "  IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as excellentRate, " +
            "  IFNULL(STDDEV(s.score), 0) as stdDev, " +
            "  COUNT(s.score) as count " +
            "FROM fact_score s " +
            "  <choose>" +
            "    <when test='dimension == \"COLLEGE\"'> JOIN dim_college c ON s.college_id = c.id </when>" +
            "    <when test='dimension == \"MAJOR\"'> JOIN dim_major m ON s.major_id = m.id </when>" +
            "    <when test='dimension == \"CLASS\"'> JOIN dim_class cl ON s.class_id = cl.id </when>" +
            "    <otherwise> JOIN dim_college c ON s.college_id = c.id </otherwise>" +
            "  </choose>" +
            "<where>" +
            "   s.is_absent = 0 " +
            "   <if test='semesterId != null'> AND s.semester_id = #{semesterId} </if>" +
            "   <if test='courseId != null'> AND s.course_id = #{courseId} </if>" +
            "   <choose>" +
            "     <when test='dimension == \"COLLEGE\"'> AND s.college_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "     <when test='dimension == \"MAJOR\"'> AND s.major_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "     <when test='dimension == \"CLASS\"'> AND s.class_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "   </choose>" +
            "</where>" +
            "GROUP BY " +
            "  <choose>" +
            "    <when test='dimension == \"COLLEGE\"'> c.id, c.name </when>" +
            "    <when test='dimension == \"MAJOR\"'> m.id, m.name </when>" +
            "    <when test='dimension == \"CLASS\"'> cl.id, cl.name </when>" +
            "    <otherwise> c.id, c.name </otherwise>" +
            "  </choose> " +
            "</script>")
    List<OlapDto.CompareMetricItem> calcCompareMetrics(@Param("dimension") String dimension, @Param("ids") List<Integer> ids, @Param("semesterId") Integer semesterId, @Param("courseId") Integer courseId, @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT " +
            "  <choose>" +
            "    <when test='dimension == \"COLLEGE\"'> s.college_id as id </when>" +
            "    <when test='dimension == \"MAJOR\"'> s.major_id as id </when>" +
            "    <when test='dimension == \"CLASS\"'> s.class_id as id </when>" +
            "    <otherwise> s.college_id as id </otherwise>" +
            "  </choose>" +
            "  , " +
            "  sum(case when s.score >= 0 and s.score &lt; 60 then 1 else 0 end) as bin1, " +
            "  sum(case when s.score >= 60 and s.score &lt; 70 then 1 else 0 end) as bin2, " +
            "  sum(case when s.score >= 70 and s.score &lt; 80 then 1 else 0 end) as bin3, " +
            "  sum(case when s.score >= 80 and s.score &lt; 90 then 1 else 0 end) as bin4, " +
            "  sum(case when s.score >= 90 and s.score &lt;= 100 then 1 else 0 end) as bin5, " +
            "  count(s.score) as total " +
            "FROM fact_score s " +
            "<where>" +
            "   s.is_absent = 0 " +
            "   <if test='semesterId != null'> AND s.semester_id = #{semesterId} </if>" +
            "   <if test='courseId != null'> AND s.course_id = #{courseId} </if>" +
            "   <choose>" +
            "     <when test='dimension == \"COLLEGE\"'> AND s.college_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "     <when test='dimension == \"MAJOR\"'> AND s.major_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "     <when test='dimension == \"CLASS\"'> AND s.class_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "   </choose>" +
            "</where>" +
            "GROUP BY " +
            "  <choose>" +
            "    <when test='dimension == \"COLLEGE\"'> s.college_id </when>" +
            "    <when test='dimension == \"MAJOR\"'> s.major_id </when>" +
            "    <when test='dimension == \"CLASS\"'> s.class_id </when>" +
            "    <otherwise> s.college_id </otherwise>" +
            "  </choose> " +
            "</script>")
    List<Map<String, Object>> calcCompareDistribution(@Param("dimension") String dimension, @Param("ids") List<Integer> ids, @Param("semesterId") Integer semesterId, @Param("courseId") Integer courseId);

    @Select("<script>" +
            "SELECT " +
            "  sm.name as semesterName," +
            "  sm.start_date as startDate," +
            "  <choose>" +
            "    <when test='dimension == \"COLLEGE\"'> s.college_id as id </when>" +
            "    <when test='dimension == \"MAJOR\"'> s.major_id as id </when>" +
            "    <when test='dimension == \"CLASS\"'> s.class_id as id </when>" +
            "    <otherwise> s.college_id as id </otherwise>" +
            "  </choose>" +
            "  ," +
            "  <choose>" +
            "    <when test='metric == \"avgScore\"'> IFNULL(AVG(s.score), 0) as value </when>" +
            "    <when test='metric == \"passRate\"'> IFNULL(SUM(CASE WHEN s.score >= #{pass} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as value </when>" +
            "    <when test='metric == \"excellentRate\"'> IFNULL(SUM(CASE WHEN s.score >= #{exc} THEN 1 ELSE 0 END) / NULLIF(COUNT(s.score), 0), 0) as value </when>" +
            "    <otherwise> IFNULL(AVG(s.score), 0) as value </otherwise>" +
            "  </choose>" +
            "FROM fact_score s " +
            "JOIN dim_semester sm ON s.semester_id = sm.id " +
            "<where>" +
            "   s.is_absent = 0 " +
            "   <if test='courseId != null'> AND s.course_id = #{courseId} </if>" +
            "   <choose>" +
            "     <when test='dimension == \"COLLEGE\"'> AND s.college_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "     <when test='dimension == \"MAJOR\"'> AND s.major_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "     <when test='dimension == \"CLASS\"'> AND s.class_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> </when>" +
            "   </choose>" +
            "</where>" +
            "GROUP BY sm.id, sm.name, sm.start_date, " +
            "  <choose>" +
            "    <when test='dimension == \"COLLEGE\"'> s.college_id </when>" +
            "    <when test='dimension == \"MAJOR\"'> s.major_id </when>" +
            "    <when test='dimension == \"CLASS\"'> s.class_id </when>" +
            "    <otherwise> s.college_id </otherwise>" +
            "  </choose> " +
            "ORDER BY sm.start_date ASC" +
            "</script>")
    List<Map<String, Object>> calcCompareTrend(@Param("dimension") String dimension, @Param("ids") List<Integer> ids, @Param("courseId") Integer courseId, @Param("metric") String metric, @Param("pass") Double pass, @Param("exc") Double exc);

    @Select("<script>" +
            "SELECT s.student_id as studentId, IFNULL(AVG(s.score), 0) as avgScore " +
            "FROM fact_score s " +
            "WHERE s.is_absent = 0 " +
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id &lt;= #{endSemesterId}) " +
            "  AND s.class_id = (SELECT ds.class_id FROM dim_student ds WHERE ds.id = #{studentId}) " +
            "  <if test='courseId != null'> AND s.course_id = #{courseId} </if>" +
            "GROUP BY s.student_id " +
            "</script>")
    List<OlapDto.StudentAvgItem> listClassStudentAveragesForStudent(@Param("studentId") Integer studentId,
                                                                     @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
                                                                     @Param("courseId") Integer courseId);

    @Select("<script>" +
            "SELECT s.student_id as studentId, IFNULL(AVG(s.score), 0) as avgScore " +
            "FROM fact_score s " +
            "WHERE s.is_absent = 0 " +
            "  AND (#{startSemesterId} IS NULL OR s.semester_id >= #{startSemesterId}) " +
            "  AND (#{endSemesterId} IS NULL OR s.semester_id &lt;= #{endSemesterId}) " +
            "  AND s.grade_id = (SELECT ds.grade_id FROM dim_student ds WHERE ds.id = #{studentId}) " +
            "  <if test='courseId != null'> AND s.course_id = #{courseId} </if>" +
            "GROUP BY s.student_id " +
            "</script>")
    List<OlapDto.StudentAvgItem> listGradeStudentAveragesForStudent(@Param("studentId") Integer studentId,
                                                                     @Param("startSemesterId") Integer startSemesterId,
            @Param("endSemesterId") Integer endSemesterId,
                                                                     @Param("courseId") Integer courseId);
}







