import random
import datetime

def generate_sql():
    sql = """-- 仅在本地测试使用，如数据库已存在会被删除重建
DROP DATABASE IF EXISTS `smart_education`;
CREATE DATABASE `smart_education` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `smart_education`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 系统配置表与权限表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '登录账户',
  `password` varchar(100) NOT NULL COMMENT '密码 (演示暂存明文)',
  `name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role_type` varchar(20) NOT NULL COMMENT 'SYS_ADMIN,SCHOOL_ADMIN,COLLEGE_ADMIN,TEACHER,STUDENT',
  `college_id` int(11) DEFAULT NULL COMMENT '所属学院(仅院级管理员/老师/学生有)',
  `student_id` int(11) DEFAULT NULL COMMENT '学生记录ID(仅学生有)',
  `teacher_id` int(11) DEFAULT NULL COMMENT '教师记录ID(仅老师有)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统账号表';

BEGIN;
INSERT INTO `sys_user` VALUES (1, 'admin', '123456', '系统管理员', 'SCHOOL_ADMIN', NULL, NULL, NULL);
INSERT INTO `sys_user` VALUES (2, 'cs_admin', '123456', '计科院管', 'COLLEGE_ADMIN', 10, NULL, NULL);
"""
    # Update teachers with format: 7000 + start
    teacher_start = 7001
    for i in range(1, 16):
        t_id = teacher_start + i - 1
        username = str(t_id) # username follows teacher ID exactly 
        sql += f"INSERT INTO `sys_user` VALUES ({3 + i - 1}, '{username}', '123456', '教师{i}', 'TEACHER', 10, NULL, {t_id});\n"
        
    # Update students with format: year(4) + college(2) + major(2) + class(4)
    # The actual student usernames will be assigned later in the script when student IDs are generated. 
    # For now, we will add a placeholder loop and replace it after dim_student is generated.
    # We will generate sys_user records for students alongside dim_student.
    
    sql += """COMMIT;

DROP TABLE IF EXISTS `sys_metric_config`;
CREATE TABLE `sys_metric_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `pass_score` double NOT NULL DEFAULT '60',
  `excellent_score` double NOT NULL DEFAULT '85',
  `score_bins` varchar(255) NOT NULL DEFAULT '["0-59","60-69","70-79","80-84","85-100"]',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标口径配置';

BEGIN;
INSERT INTO `sys_metric_config` VALUES (1, 60.0, 85.0, '["0-59","60-69","70-79","80-84","85-100"]');
COMMIT;

-- ----------------------------
-- 2. 维度表
-- ----------------------------
DROP TABLE IF EXISTS `dim_semester`;
CREATE TABLE `dim_semester` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期维度表';

BEGIN;
"""
    # 15 semesters
    for i in range(2009, 2024):
        sql += f"INSERT INTO `dim_semester` VALUES ({i - 2008}, '{i}-Fall', '{i}-09-01', '{i+1}-01-15');\n"
        sql += f"INSERT INTO `dim_semester` VALUES ({i - 2008 + 15}, '{i+1}-Spring', '{i+1}-02-25', '{i+1}-07-10');\n"
        
    sql += """COMMIT;

DROP TABLE IF EXISTS `dim_college`;
CREATE TABLE `dim_college` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院维度表';

BEGIN;
"""
    colleges = ['计算机学院', '机械学院', '外语学院', '理学院', '化工学院', '经济管理学院', '法学院', '艺术学院', '体育学院', '电子信息学院', '材料学院', '环境学院', '生命科学学院', '医学院', '航空航天学院', '建筑学院']
    college_ids = []
    for i, name in enumerate(colleges):
        c_id = (i+1)*10
        college_ids.append(c_id)
        sql += f"INSERT INTO `dim_college` VALUES ({c_id}, '{name}');\n"
        
    sql += """COMMIT;

DROP TABLE IF EXISTS `dim_major`;
CREATE TABLE `dim_major` (
  `id` int(11) NOT NULL,
  `college_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业维度表';

BEGIN;
"""
    major_ids = []
    for i, c_id in enumerate(college_ids):
        m_id1 = (i+1)*100 + 1
        m_id2 = (i+1)*100 + 2
        major_ids.extend([m_id1, m_id2])
        sql += f"INSERT INTO `dim_major` VALUES ({m_id1}, {c_id}, '{colleges[i][:2]}专业1');\n"
        sql += f"INSERT INTO `dim_major` VALUES ({m_id2}, {c_id}, '{colleges[i][:2]}专业2');\n"

    sql += """COMMIT;

DROP TABLE IF EXISTS `dim_grade`;
CREATE TABLE `dim_grade` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级维度表';

BEGIN;
"""
    grade_ids = []
    for i in range(2005, 2025):
        grade_ids.append(i)
        sql += f"INSERT INTO `dim_grade` VALUES ({i}, '{i}级');\n"
        
    sql += """COMMIT;

DROP TABLE IF EXISTS `dim_class`;
CREATE TABLE `dim_class` (
  `id` int(11) NOT NULL,
  `grade_id` int(11) NOT NULL,
  `major_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级维度表';

BEGIN;
"""
    class_ids = []
    for i in range(1, 21):
        c_id = 9000 + i
        grade = random.choice(grade_ids)
        major = random.choice(major_ids)
        class_ids.append(c_id)
        sql += f"INSERT INTO `dim_class` VALUES ({c_id}, {grade}, {major}, '班级{i}');\n"
        
    sql += """COMMIT;

DROP TABLE IF EXISTS `dim_teacher`;
CREATE TABLE `dim_teacher` (
  `id` int(11) NOT NULL,
  `college_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `title` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师维度表';

BEGIN;
"""
    teacher_ids = []
    for i in range(1, 21):
        t_id = 7000 + i
        teacher_ids.append(t_id)
        college = random.choice(college_ids)
        title = random.choice(['教授', '副教授', '讲师', '助教'])
        sql += f"INSERT INTO `dim_teacher` VALUES ({t_id}, {college}, '教师{i}', '{title}');\n"
        
    sql += """COMMIT;

DROP TABLE IF EXISTS `dim_course`;
CREATE TABLE `dim_course` (
  `id` int(11) NOT NULL,
  `college_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `credit` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程维度表';

BEGIN;
"""
    course_ids = []
    for i in range(1, 21):
        c_id = 3000 + i
        course_ids.append(c_id)
        college = random.choice(college_ids)
        credit = random.choice([2.0, 3.0, 3.5, 4.0, 5.0])
        sql += f"INSERT INTO `dim_course` VALUES ({c_id}, {college}, '课程{i}', {credit});\n"

    sql += """COMMIT;

DROP TABLE IF EXISTS `dim_student`;
CREATE TABLE `dim_student` (
  `id` int(11) NOT NULL,
  `student_no` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `college_id` int(11) NOT NULL,
  `major_id` int(11) NOT NULL,
  `grade_id` int(11) NOT NULL,
  `class_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生维度表';

BEGIN;
"""
    student_ids = []
    students_info = {}
    
    # Need to keep track of user ID for sys_user inserts
    user_id = 18 
    
    # List to hold sys_user student inserts
    sys_user_student_inserts = []
    
    for i in range(1, 31):
        s_id = 50000 + i
        student_ids.append(s_id)
        year = random.choice(range(2020, 2024))
        
        college = random.choice(college_ids)
        major = random.choice([m for m in major_ids if str(m).startswith(str(college)[:-1])])
        grade = year
        cls = random.choice(class_ids)
        
        # New student_no format: year(4) + college(2) + major(2) + class(4)
        c_code = f"{college//10:02d}"
        m_code = f"{major%100:02d}"
        cls_code = f"{cls%100:04d}"
        s_no = f"{year}{c_code}{m_code}{cls_code}"
        
        students_info[s_id] = {'college': college, 'major': major, 'grade': grade, 'class': cls}
        sql += f"INSERT INTO `dim_student` VALUES ({s_id}, '{s_no}', '学生{i}', {college}, {major}, {grade}, {cls});\n"
        
        sys_user_student_inserts.append(f"INSERT INTO `sys_user` VALUES ({user_id}, '{s_no}', '123456', '学生{i}', 'STUDENT', {college}, {s_id}, NULL);\n")
        user_id += 1

    sql += "COMMIT;\n\n"
    
    # Now append sys_user student inserts since they depend on dim_student generation
    sql += "BEGIN;\n"
    for insert in sys_user_student_inserts:
        sql += insert
    sql += "COMMIT;\n\n"
    
    sql += """-- ----------------------------
-- 3. OLTP 事实表
-- ----------------------------
DROP TABLE IF EXISTS `fact_score`;
CREATE TABLE `fact_score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `semester_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `teacher_id` int(11) NOT NULL,
  `score` double DEFAULT NULL COMMENT '成绩',
  `is_absent` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否缺考，1缺考 0否',
  -- 为了OLAP分析方便，将常用外键冗余存储
  `college_id` int(11) NOT NULL,
  `major_id` int(11) NOT NULL,
  `grade_id` int(11) NOT NULL,
  `class_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_student` (`student_id`),
  KEY `idx_course` (`course_id`),
  KEY `idx_semester_college` (`semester_id`,`college_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩明细事实表';

BEGIN;
"""
    for i in range(1, 51):
        semester = random.randint(1, 15)
        student = random.choice(student_ids)
        course = random.choice(course_ids)
        teacher = random.choice(teacher_ids)
        is_absent = 1 if random.random() < 0.05 else 0
        score = "NULL" if is_absent else round(random.uniform(40, 100), 1)
        info = students_info[student]
        sql += f"INSERT INTO `fact_score` VALUES ({i}, {semester}, {student}, {course}, {teacher}, {score}, {is_absent}, {info['college']}, {info['major']}, {info['grade']}, {info['class']});\n"

    sql += """COMMIT;

DROP TABLE IF EXISTS `fact_enroll`;
CREATE TABLE `fact_enroll` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `semester_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `teacher_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `college_id` int(11) NOT NULL,
  `major_id` int(11) NOT NULL,
  `grade_id` int(11) NOT NULL,
  `is_drop` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否退课 1是 0否',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课明细表';

BEGIN;
"""
    for i in range(1, 31):
        semester = random.randint(1, 15)
        student = random.choice(student_ids)
        course = random.choice(course_ids)
        teacher = random.choice(teacher_ids)
        is_drop = 1 if random.random() < 0.1 else 0
        info = students_info[student]
        sql += f"INSERT INTO `fact_enroll` VALUES ({i}, {semester}, {course}, {teacher}, {student}, {info['college']}, {info['major']}, {info['grade']}, {is_drop});\n"

    sql += """COMMIT;

-- ----------------------------
-- 4. 导入任务表
-- ----------------------------
DROP TABLE IF EXISTS `sys_import_task`;
CREATE TABLE `sys_import_task` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(20) NOT NULL COMMENT 'SCORE, ENROLL',
  `semester_id` int(11) DEFAULT NULL,
  `status` varchar(20) NOT NULL COMMENT 'PENDING, SUCCESS, FAILED',
  `total_rows` int(11) NOT NULL DEFAULT '0',
  `success_rows` int(11) NOT NULL DEFAULT '0',
  `failed_rows` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入任务记录';

BEGIN;
"""
    for i in range(1001, 1016):
        type_str = random.choice(['SCORE', 'ENROLL'])
        status = random.choice(['SUCCESS', 'FAILED', 'PENDING'])
        sql += f"INSERT INTO `sys_import_task` VALUES ({i}, '{type_str}', 3, '{status}', 1000, 998, 2, '2026-03-02 10:00:00');\n"

    sql += """COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
"""
    with open('init.sql', 'w', encoding='utf-8') as f:
        f.write(sql)
    print("Successfully generated init.sql")

if __name__ == "__main__":
    generate_sql()
