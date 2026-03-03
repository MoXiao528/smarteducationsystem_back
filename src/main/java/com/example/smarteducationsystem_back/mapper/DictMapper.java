package com.example.smarteducationsystem_back.mapper;

import com.example.smarteducationsystem_back.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictMapper {
    @Select("SELECT * FROM dim_semester ORDER BY start_date DESC")
    List<DimSemester> findAllSemesters();

    @Select("SELECT * FROM dim_college ORDER BY id ASC")
    List<DimCollege> findAllColleges();

    @Select("<script>" +
            "SELECT * FROM dim_major " +
            "<where> " +
            "<if test='collegeId != null'> AND college_id = #{collegeId} </if>" +
            "</where> ORDER BY id ASC" +
            "</script>")
    List<DimMajor> findMajors(@Param("collegeId") Integer collegeId);

    @Select("SELECT * FROM dim_grade ORDER BY id DESC")
    List<DimGrade> findAllGrades();

    @Select("<script>" +
            "SELECT * FROM dim_class " +
            "<where> " +
            "<if test='gradeId != null'> AND grade_id = #{gradeId} </if>" +
            "<if test='majorId != null'> AND major_id = #{majorId} </if>" +
            "</where> ORDER BY id ASC" +
            "</script>")
    List<DimClass> findClasses(@Param("gradeId") Integer gradeId, @Param("majorId") Integer majorId);

    @Select("<script>" +
            "SELECT DISTINCT c.* FROM dim_course c " +
            "<if test='semesterId != null'> JOIN fact_score fs ON c.id = fs.course_id </if>" +
            "<where> " +
            "<if test='semesterId != null'> fs.semester_id = #{semesterId} </if>" +
            "<if test='collegeId != null'> AND c.college_id = #{collegeId} </if>" +
            "</where> ORDER BY c.id ASC" +
            "</script>")
    List<DimCourse> findCourses(@Param("semesterId") Integer semesterId, @Param("collegeId") Integer collegeId, @Param("majorId") Integer majorId);
    
    @Select("SELECT * FROM dim_teacher ORDER BY id ASC")
    List<DimTeacher> findAllTeachers();
}
