package com.example.smarteducationsystem_back.mapper;

import com.example.smarteducationsystem_back.dto.ProfileDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProfileMapper {

    @Select("SELECT s.name, s.student_no as studentNo, c.name as collegeName, m.name as majorName, " +
            "g.name as gradeName, cl.name as className " +
            "FROM dim_student s " +
            "LEFT JOIN dim_college c ON s.college_id = c.id " +
            "LEFT JOIN dim_major m ON s.major_id = m.id " +
            "LEFT JOIN dim_grade g ON s.grade_id = g.id " +
            "LEFT JOIN dim_class cl ON s.class_id = cl.id " +
            "WHERE s.id = #{studentId}")
    ProfileDto.StudentProfile getStudentProfile(@Param("studentId") Integer studentId);

    @Select("SELECT t.name, t.title FROM dim_teacher t WHERE t.id = #{teacherId}")
    ProfileDto.TeacherProfile getTeacherProfile(@Param("teacherId") Integer teacherId);

    @Select("SELECT DISTINCT c.name " +
            "FROM fact_score fs " +
            "JOIN dim_course c ON fs.course_id = c.id " +
            "WHERE fs.teacher_id = #{teacherId}")
    List<String> getTeacherCourses(@Param("teacherId") Integer teacherId);
}
