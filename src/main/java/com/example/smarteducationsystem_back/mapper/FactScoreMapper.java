package com.example.smarteducationsystem_back.mapper;

import com.example.smarteducationsystem_back.dto.ScoreDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FactScoreMapper {

    @Select("<script>" +
            "SELECT " +
            "   s.id, " +
            "   s.semester_id as semesterId, " +
            "   s.college_id as collegeId, " +
            "   s.major_id as majorId, " +
            "   s.grade_id as gradeId, " +
            "   s.class_id as classId, " +
            "   s.student_id as studentId, " +
            "   stu.student_no as studentNo, " +
            "   stu.name as studentName, " +
            "   s.course_id as courseId, " +
            "   c.name as courseName, " +
            "   s.teacher_id as teacherId, " +
            "   t.name as teacherName, " +
            "   s.score as score, " +
            "   s.is_absent as isAbsent " +
            "FROM fact_score s " +
            "LEFT JOIN dim_student stu ON s.student_id = stu.id " +
            "LEFT JOIN dim_course c ON s.course_id = c.id " +
            "LEFT JOIN dim_teacher t ON s.teacher_id = t.id " +
            "<where> " +
            "   <if test='req.semesterId != null'> AND s.semester_id = #{req.semesterId} </if>" +
            "   <if test='req.collegeId != null'> AND s.college_id = #{req.collegeId} </if>" +
            "   <if test='req.majorId != null'> AND s.major_id = #{req.majorId} </if>" +
            "   <if test='req.gradeId != null'> AND s.grade_id = #{req.gradeId} </if>" +
            "   <if test='req.classId != null'> AND s.class_id = #{req.classId} </if>" +
            "   <if test='req.courseId != null'> AND s.course_id = #{req.courseId} </if>" +
            "   <if test='req.studentId != null'> AND s.student_id = #{req.studentId} </if>" +
            "   <if test='req.studentKey != null and req.studentKey != \"\"'>" +
            "       AND (stu.name LIKE CONCAT('%', #{req.studentKey}, '%') OR stu.student_no LIKE CONCAT('%', #{req.studentKey}, '%')) " +
            "   </if>" +
            "   <if test='req.scoreMin != null'> AND s.score &gt;= #{req.scoreMin} </if>" +
            "   <if test='req.scoreMax != null'> AND s.score &lt;= #{req.scoreMax} </if>" +
            "</where>" +
            "</script>")
    List<ScoreDto.Item> selectScores(@Param("req") ScoreDto.Req req);
}
