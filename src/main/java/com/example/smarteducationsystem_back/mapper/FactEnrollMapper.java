package com.example.smarteducationsystem_back.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FactEnrollMapper {

    @Select("<script>" +
            "SELECT e.id, c.name as courseName, c.credit as courseCredit, " +
            "t.name as teacherName, s.name as studentName, s.student_no as studentNo, " +
            "col.name as collegeName, m.name as majorName, g.name as gradeName " +
            "FROM fact_enroll e " +
            "JOIN dim_course c ON e.course_id = c.id " +
            "JOIN dim_teacher t ON e.teacher_id = t.id " +
            "JOIN dim_student s ON e.student_id = s.id " +
            "JOIN dim_college col ON e.college_id = col.id " +
            "JOIN dim_major m ON e.major_id = m.id " +
            "JOIN dim_grade g ON e.grade_id = g.id " +
            "<where> " +
            "   <if test='collegeId != null'> AND e.college_id = #{collegeId} </if>" +
            "   <if test='teacherId != null'> AND e.teacher_id = #{teacherId} </if>" +
            "   <if test='studentId != null'> AND e.student_id = #{studentId} </if>" +
            "   <if test='courseId != null'> AND e.course_id = #{courseId} </if>" +
            "</where>" +
            "</script>")
    List<Map<String, Object>> selectEnrolls(@Param("collegeId") Integer collegeId, 
                                            @Param("teacherId") Integer teacherId, 
                                            @Param("studentId") Integer studentId,
                                            @Param("courseId") Integer courseId);

    @Select("<script>" +
            "SELECT c.id, c.name, c.credit, col.name as collegeName, " +
            "COUNT(DISTINCT e.student_id) as studentCount " +
            "FROM dim_course c " +
            "LEFT JOIN dim_college col ON c.college_id = col.id " +
            "LEFT JOIN fact_enroll e ON c.id = e.course_id " +
            "<where> " +
            "   <if test='collegeId != null'> AND c.college_id = #{collegeId} </if>" +
            "   <if test='teacherId != null'> AND c.id IN (SELECT DISTINCT course_id FROM fact_enroll WHERE teacher_id = #{teacherId}) </if>" +
            "</where>" +
            "GROUP BY c.id, c.name, c.credit, col.name" +
            "</script>")
    List<Map<String, Object>> selectCoursesWithEnrollCount(@Param("collegeId") Integer collegeId,
                                                           @Param("teacherId") Integer teacherId);

    @Select("<script>" +
            "SELECT COUNT(DISTINCT e.course_id) FROM fact_enroll e " +
            "<where> " +
            "   <if test='semesterId != null'> AND e.semester_id = #{semesterId} </if>" +
            "   <if test='collegeId != null'> AND e.college_id = #{collegeId} </if>" +
            "   <if test='majorId != null'> AND e.major_id = #{majorId} </if>" +
            "</where>" +
            "</script>")
    Integer countOpenCourses(@Param("semesterId") Integer semesterId,
                             @Param("collegeId") Integer collegeId,
                             @Param("majorId") Integer majorId);

    @Select("<script>" +
            "SELECT COUNT(*) FROM fact_enroll e " +
            "<where> " +
            "   <if test='semesterId != null'> AND e.semester_id = #{semesterId} </if>" +
            "   <if test='collegeId != null'> AND e.college_id = #{collegeId} </if>" +
            "   <if test='majorId != null'> AND e.major_id = #{majorId} </if>" +
            "</where>" +
            "</script>")
    Integer countEnrolls(@Param("semesterId") Integer semesterId,
                         @Param("collegeId") Integer collegeId,
                         @Param("majorId") Integer majorId);

    @Select("<script>" +
            "SELECT " +
            " c.id AS courseId, " +
            " c.name AS courseName, " +
            " t.name AS teacherName, " +
            " COUNT(e.id) AS enroll, " +
            " 200 AS capacity, " + // Demo value for capacity
            " SUM(CASE WHEN e.is_drop = 1 THEN 1 ELSE 0 END) / COUNT(e.id) AS dropRate " +
            "FROM fact_enroll e " +
            "JOIN dim_course c ON e.course_id = c.id " +
            "JOIN dim_teacher t ON e.teacher_id = t.id " +
            "<where> " +
            "   <if test='semesterId != null'> AND e.semester_id = #{semesterId} </if>" +
            "   <if test='collegeId != null'> AND e.college_id = #{collegeId} </if>" +
            "   <if test='majorId != null'> AND e.major_id = #{majorId} </if>" +
            "</where>" +
            "GROUP BY c.id, c.name, t.name " +
            "ORDER BY enroll DESC " +
            "LIMIT 5" +
            "</script>")
    List<Map<String, Object>> calcHotCourses(@Param("semesterId") Integer semesterId,
                                             @Param("collegeId") Integer collegeId,
                                             @Param("majorId") Integer majorId);

    @Select("<script>" +
            "SELECT m.id, m.name, COUNT(e.id) AS value " +
            "FROM fact_enroll e " +
            "JOIN dim_major m ON e.major_id = m.id " +
            "<where> " +
            "   <if test='semesterId != null'> AND e.semester_id = #{semesterId} </if>" +
            "   <if test='collegeId != null'> AND e.college_id = #{collegeId} </if>" +
            "   <if test='majorId != null'> AND e.major_id = #{majorId} </if>" +
            "</where>" +
            "GROUP BY m.id, m.name " +
            "ORDER BY value DESC" +
            "</script>")
    List<Map<String, Object>> calcMajorDistribution(@Param("semesterId") Integer semesterId,
                                                    @Param("collegeId") Integer collegeId,
                                                    @Param("majorId") Integer majorId);

    @Select("<script>" +
            "SELECT g.id, g.name, COUNT(e.id) AS value " +
            "FROM fact_enroll e " +
            "JOIN dim_grade g ON e.grade_id = g.id " +
            "<where> " +
            "   <if test='semesterId != null'> AND e.semester_id = #{semesterId} </if>" +
            "   <if test='collegeId != null'> AND e.college_id = #{collegeId} </if>" +
            "   <if test='majorId != null'> AND e.major_id = #{majorId} </if>" +
            "</where>" +
            "GROUP BY g.id, g.name " +
            "ORDER BY g.name ASC" +
            "</script>")
    List<Map<String, Object>> calcGradeDistribution(@Param("semesterId") Integer semesterId,
                                                    @Param("collegeId") Integer collegeId,
                                                    @Param("majorId") Integer majorId);
}
