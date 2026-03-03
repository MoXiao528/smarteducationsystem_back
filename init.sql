-- 仅在本地测试使用，如数据库已存在会被删除重建
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
INSERT INTO `sys_user` VALUES (3, '7001', '123456', '教师1', 'TEACHER', 10, NULL, 7001);
INSERT INTO `sys_user` VALUES (4, '7002', '123456', '教师2', 'TEACHER', 10, NULL, 7002);
INSERT INTO `sys_user` VALUES (5, '7003', '123456', '教师3', 'TEACHER', 10, NULL, 7003);
INSERT INTO `sys_user` VALUES (6, '202101010001', '123456', '学生1', 'STUDENT', 10, 50001, NULL);
INSERT INTO `sys_user` VALUES (7, '202101010002', '123456', '学生2', 'STUDENT', 10, 50002, NULL);
INSERT INTO `sys_user` VALUES (8, '202101010003', '123456', '学生3', 'STUDENT', 10, 50003, NULL);
INSERT INTO `sys_user` VALUES (9, '202201020004', '123456', '学生4', 'STUDENT', 10, 50004, NULL);
INSERT INTO `sys_user` VALUES (10, '202102010005', '123456', '学生5', 'STUDENT', 10, 50005, NULL);
INSERT INTO `sys_user` VALUES (11, '202203010006', '123456', '学生6', 'STUDENT', 10, 50006, NULL);
INSERT INTO `sys_user` VALUES (12, '202104010007', '123456', '学生7', 'STUDENT', 10, 50007, NULL);
INSERT INTO `sys_user` VALUES (13, '202105010008', '123456', '学生8', 'STUDENT', 10, 50008, NULL);
INSERT INTO `sys_user` VALUES (14, '202206010009', '123456', '学生9', 'STUDENT', 10, 50009, NULL);
INSERT INTO `sys_user` VALUES (15, '202107010010', '123456', '学生10', 'STUDENT', 10, 50010, NULL);
INSERT INTO `sys_user` VALUES (16, '202108010011', '123456', '学生11', 'STUDENT', 10, 50011, NULL);
INSERT INTO `sys_user` VALUES (17, '202109010012', '123456', '学生12', 'STUDENT', 10, 50012, NULL);
INSERT INTO `sys_user` VALUES (18, '202110010013', '123456', '学生13', 'STUDENT', 10, 50013, NULL);
INSERT INTO `sys_user` VALUES (19, '202211010014', '123456', '学生14', 'STUDENT', 10, 50014, NULL);
INSERT INTO `sys_user` VALUES (20, '202112010015', '123456', '学生15', 'STUDENT', 10, 50015, NULL);
COMMIT;


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
INSERT INTO `dim_semester` VALUES (1, '2016-Fall', '2016-09-01', '2017-01-15');
INSERT INTO `dim_semester` VALUES (2, '2017-Spring', '2017-02-25', '2017-07-10');
INSERT INTO `dim_semester` VALUES (3, '2017-Fall', '2017-09-01', '2018-01-15');
INSERT INTO `dim_semester` VALUES (4, '2018-Spring', '2018-02-25', '2018-07-10');
INSERT INTO `dim_semester` VALUES (5, '2018-Fall', '2018-09-01', '2019-01-15');
INSERT INTO `dim_semester` VALUES (6, '2019-Spring', '2019-02-25', '2019-07-10');
INSERT INTO `dim_semester` VALUES (7, '2019-Fall', '2019-09-01', '2020-01-15');
INSERT INTO `dim_semester` VALUES (8, '2020-Spring', '2020-02-25', '2020-07-10');
INSERT INTO `dim_semester` VALUES (9, '2020-Fall', '2020-09-01', '2021-01-15');
INSERT INTO `dim_semester` VALUES (10, '2021-Spring', '2021-02-25', '2021-07-10');
INSERT INTO `dim_semester` VALUES (11, '2021-Fall', '2021-09-01', '2022-01-15');
INSERT INTO `dim_semester` VALUES (12, '2022-Spring', '2022-02-25', '2022-07-10');
INSERT INTO `dim_semester` VALUES (13, '2022-Fall', '2022-09-01', '2023-01-15');
INSERT INTO `dim_semester` VALUES (14, '2023-Spring', '2023-02-25', '2023-07-10');
INSERT INTO `dim_semester` VALUES (15, '2023-Fall', '2023-09-01', '2024-01-15');
COMMIT;


DROP TABLE IF EXISTS `dim_college`;
CREATE TABLE `dim_college` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院维度表';

BEGIN;
INSERT INTO `dim_college` VALUES (10, '计算机学院');
INSERT INTO `dim_college` VALUES (20, '机械学院');
INSERT INTO `dim_college` VALUES (30, '外语学院');
INSERT INTO `dim_college` VALUES (40, '理学院');
INSERT INTO `dim_college` VALUES (50, '化工学院');
INSERT INTO `dim_college` VALUES (60, '经济管理学院');
INSERT INTO `dim_college` VALUES (70, '法学院');
INSERT INTO `dim_college` VALUES (80, '艺术学院');
INSERT INTO `dim_college` VALUES (90, '体育学院');
INSERT INTO `dim_college` VALUES (100, '电子信息学院');
INSERT INTO `dim_college` VALUES (110, '材料学院');
INSERT INTO `dim_college` VALUES (120, '环境学院');
INSERT INTO `dim_college` VALUES (130, '生命科学学院');
INSERT INTO `dim_college` VALUES (140, '医学院');
INSERT INTO `dim_college` VALUES (150, '航空航天学院');
COMMIT;


DROP TABLE IF EXISTS `dim_major`;
CREATE TABLE `dim_major` (
  `id` int(11) NOT NULL,
  `college_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业维度表';

BEGIN;
INSERT INTO `dim_major` VALUES (101, 10, '计算机科学与技术');
INSERT INTO `dim_major` VALUES (102, 10, '软件工程');
INSERT INTO `dim_major` VALUES (201, 20, '机械设计与制造');
INSERT INTO `dim_major` VALUES (301, 30, '英语');
INSERT INTO `dim_major` VALUES (401, 40, '数学与应用数学');
INSERT INTO `dim_major` VALUES (501, 50, '化学工程与工艺');
INSERT INTO `dim_major` VALUES (601, 60, '工商管理');
INSERT INTO `dim_major` VALUES (701, 70, '法学');
INSERT INTO `dim_major` VALUES (801, 80, '视觉传达设计');
INSERT INTO `dim_major` VALUES (901, 90, '体育教育');
INSERT INTO `dim_major` VALUES (1001, 100, '电子信息工程');
INSERT INTO `dim_major` VALUES (1101, 110, '材料科学与工程');
INSERT INTO `dim_major` VALUES (1201, 120, '环境工程');
INSERT INTO `dim_major` VALUES (1301, 130, '生物技术');
INSERT INTO `dim_major` VALUES (1401, 140, '临床医学');
COMMIT;


DROP TABLE IF EXISTS `dim_grade`;
CREATE TABLE `dim_grade` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级维度表';

BEGIN;
INSERT INTO `dim_grade` VALUES (2009, '2009级');
INSERT INTO `dim_grade` VALUES (2010, '2010级');
INSERT INTO `dim_grade` VALUES (2011, '2011级');
INSERT INTO `dim_grade` VALUES (2012, '2012级');
INSERT INTO `dim_grade` VALUES (2013, '2013级');
INSERT INTO `dim_grade` VALUES (2014, '2014级');
INSERT INTO `dim_grade` VALUES (2015, '2015级');
INSERT INTO `dim_grade` VALUES (2016, '2016级');
INSERT INTO `dim_grade` VALUES (2017, '2017级');
INSERT INTO `dim_grade` VALUES (2018, '2018级');
INSERT INTO `dim_grade` VALUES (2019, '2019级');
INSERT INTO `dim_grade` VALUES (2020, '2020级');
INSERT INTO `dim_grade` VALUES (2021, '2021级');
INSERT INTO `dim_grade` VALUES (2022, '2022级');
INSERT INTO `dim_grade` VALUES (2023, '2023级');
COMMIT;


DROP TABLE IF EXISTS `dim_class`;
CREATE TABLE `dim_class` (
  `id` int(11) NOT NULL,
  `grade_id` int(11) NOT NULL,
  `major_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级维度表';

BEGIN;
INSERT INTO `dim_class` VALUES (9001, 2021, 101, '计科21-1班');
INSERT INTO `dim_class` VALUES (9002, 2021, 101, '计科21-2班');
INSERT INTO `dim_class` VALUES (9003, 2022, 102, '软工22-1班');
INSERT INTO `dim_class` VALUES (9004, 2021, 201, '机械21-1班');
INSERT INTO `dim_class` VALUES (9005, 2022, 301, '英语22-1班');
INSERT INTO `dim_class` VALUES (9006, 2021, 401, '数学21-1班');
INSERT INTO `dim_class` VALUES (9007, 2021, 501, '化工21-1班');
INSERT INTO `dim_class` VALUES (9008, 2022, 601, '工管22-1班');
INSERT INTO `dim_class` VALUES (9009, 2021, 701, '法学21-1班');
INSERT INTO `dim_class` VALUES (9010, 2021, 801, '设计21-1班');
INSERT INTO `dim_class` VALUES (9011, 2021, 901, '体教21-1班');
INSERT INTO `dim_class` VALUES (9012, 2021, 1001, '电子21-1班');
INSERT INTO `dim_class` VALUES (9013, 2022, 1101, '材料22-1班');
INSERT INTO `dim_class` VALUES (9014, 2021, 1201, '环境21-1班');
INSERT INTO `dim_class` VALUES (9015, 2022, 1301, '生技22-1班');
COMMIT;


DROP TABLE IF EXISTS `dim_teacher`;
CREATE TABLE `dim_teacher` (
  `id` int(11) NOT NULL,
  `college_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `title` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师维度表';

BEGIN;
INSERT INTO `dim_teacher` VALUES (7001, 10, '李老师', '副教授');
INSERT INTO `dim_teacher` VALUES (7002, 10, '王老师', '讲师');
INSERT INTO `dim_teacher` VALUES (7003, 20, '张老师', '教授');
INSERT INTO `dim_teacher` VALUES (7004, 30, '刘老师', '助教');
INSERT INTO `dim_teacher` VALUES (7005, 40, '陈老师', '副教授');
INSERT INTO `dim_teacher` VALUES (7006, 50, '杨老师', '讲师');
INSERT INTO `dim_teacher` VALUES (7007, 60, '赵老师', '教授');
INSERT INTO `dim_teacher` VALUES (7008, 70, '黄老师', '副教授');
INSERT INTO `dim_teacher` VALUES (7009, 80, '周老师', '讲师');
INSERT INTO `dim_teacher` VALUES (7010, 90, '吴老师', '教授');
INSERT INTO `dim_teacher` VALUES (7011, 100, '徐老师', '助教');
INSERT INTO `dim_teacher` VALUES (7012, 110, '孙老师', '副教授');
INSERT INTO `dim_teacher` VALUES (7013, 120, '马老师', '讲师');
INSERT INTO `dim_teacher` VALUES (7014, 130, '朱老师', '教授');
INSERT INTO `dim_teacher` VALUES (7015, 140, '胡老师', '讲师');
COMMIT;


DROP TABLE IF EXISTS `dim_course`;
CREATE TABLE `dim_course` (
  `id` int(11) NOT NULL,
  `college_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `credit` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程维度表';

BEGIN;
INSERT INTO `dim_course` VALUES (3001, 10, '数据结构', 4.0);
INSERT INTO `dim_course` VALUES (3002, 10, '计算机网络', 3.5);
INSERT INTO `dim_course` VALUES (3003, 20, '机械制图', 3.0);
INSERT INTO `dim_course` VALUES (3004, 30, '高级英语', 2.0);
INSERT INTO `dim_course` VALUES (3005, 40, '高等数学', 5.0);
INSERT INTO `dim_course` VALUES (3006, 50, '化工原理', 3.5);
INSERT INTO `dim_course` VALUES (3007, 60, '微观经济学', 3.0);
INSERT INTO `dim_course` VALUES (3008, 70, '法理学', 2.5);
INSERT INTO `dim_course` VALUES (3009, 80, '色彩构成', 2.0);
INSERT INTO `dim_course` VALUES (3010, 90, '体育学', 1.0);
INSERT INTO `dim_course` VALUES (3011, 100, '数字电路', 4.0);
INSERT INTO `dim_course` VALUES (3012, 110, '材料力学', 3.5);
INSERT INTO `dim_course` VALUES (3013, 120, '环境监测', 3.0);
INSERT INTO `dim_course` VALUES (3014, 130, '分子生物学', 4.0);
INSERT INTO `dim_course` VALUES (3015, 140, '系统解剖学', 5.0);
COMMIT;


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
INSERT INTO `dim_student` VALUES (50001, '202101010001', '张三', 10, 101, 2021, 9001);
INSERT INTO `dim_student` VALUES (50002, '202101010002', '李四', 10, 101, 2021, 9001);
INSERT INTO `dim_student` VALUES (50003, '202101010003', '王五', 10, 101, 2021, 9002);
INSERT INTO `dim_student` VALUES (50004, '202201020004', '赵六', 10, 102, 2022, 9003);
INSERT INTO `dim_student` VALUES (50005, '202102010005', '钱七', 20, 201, 2021, 9004);
INSERT INTO `dim_student` VALUES (50006, '202203010006', '周八', 30, 301, 2022, 9005);
INSERT INTO `dim_student` VALUES (50007, '202104010007', '吴九', 40, 401, 2021, 9006);
INSERT INTO `dim_student` VALUES (50008, '202105010008', '郑十', 50, 501, 2021, 9007);
INSERT INTO `dim_student` VALUES (50009, '202206010009', '林十一', 60, 601, 2022, 9008);
INSERT INTO `dim_student` VALUES (50010, '202107010010', '何十二', 70, 701, 2021, 9009);
INSERT INTO `dim_student` VALUES (50011, '202108010011', '刘十三', 80, 801, 2021, 9010);
INSERT INTO `dim_student` VALUES (50012, '202109010012', '陈十四', 90, 901, 2021, 9011);
INSERT INTO `dim_student` VALUES (50013, '202110010013', '杨十五', 100, 1001, 2021, 9012);
INSERT INTO `dim_student` VALUES (50014, '202211010014', '黄十六', 110, 1101, 2022, 9013);
INSERT INTO `dim_student` VALUES (50015, '202112010015', '徐十七', 120, 1201, 2021, 9014);
COMMIT;


-- ----------------------------
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
INSERT INTO `fact_score` VALUES (1, 15, 50001, 3001, 7001, 86.0, 0, 10, 101, 2021, 9001);
INSERT INTO `fact_score` VALUES (2, 14, 50001, 3002, 7002, 92.5, 0, 10, 101, 2021, 9001);
INSERT INTO `fact_score` VALUES (3, 13, 50001, 3001, 7001, 88.0, 0, 10, 101, 2021, 9001);
INSERT INTO `fact_score` VALUES (4, 15, 50002, 3001, 7001, 75.0, 0, 10, 101, 2021, 9001);
INSERT INTO `fact_score` VALUES (5, 14, 50002, 3002, 7002, 58.0, 0, 10, 101, 2021, 9001);
INSERT INTO `fact_score` VALUES (6, 13, 50002, 3001, 7001, NULL, 1, 10, 101, 2021, 9001); 
INSERT INTO `fact_score` VALUES (7, 15, 50003, 3001, 7001, 62.0, 0, 10, 101, 2021, 9002);
INSERT INTO `fact_score` VALUES (8, 14, 50003, 3002, 7002, 80.0, 0, 10, 101, 2021, 9002);
INSERT INTO `fact_score` VALUES (9, 14, 50004, 3001, 7001, 95.0, 0, 10, 102, 2022, 9003);
INSERT INTO `fact_score` VALUES (10, 13, 50004, 3002, 7002, 89.0, 0, 10, 102, 2022, 9003);
INSERT INTO `fact_score` VALUES (11, 15, 50005, 3003, 7003, 77.0, 0, 20, 201, 2021, 9004);
INSERT INTO `fact_score` VALUES (12, 14, 50006, 3004, 7004, 66.0, 0, 30, 301, 2022, 9005);
INSERT INTO `fact_score` VALUES (13, 13, 50007, 3005, 7005, 91.0, 0, 40, 401, 2021, 9006);
INSERT INTO `fact_score` VALUES (14, 15, 50008, 3006, 7006, 85.0, 0, 50, 501, 2021, 9007);
INSERT INTO `fact_score` VALUES (15, 14, 50009, 3007, 7007, 93.0, 0, 60, 601, 2022, 9008);
INSERT INTO `fact_score` VALUES (16, 13, 50010, 3008, 7008, 82.0, 0, 70, 701, 2021, 9009);
INSERT INTO `fact_score` VALUES (17, 15, 50011, 3009, 7009, 78.0, 0, 80, 801, 2021, 9010);
INSERT INTO `fact_score` VALUES (18, 14, 50012, 3010, 7010, 60.0, 0, 90, 901, 2021, 9011);
INSERT INTO `fact_score` VALUES (19, 13, 50013, 3011, 7011, 100.0, 0, 100, 1001, 2021, 9012);
INSERT INTO `fact_score` VALUES (20, 15, 50014, 3012, 7012, 88.0, 0, 110, 1101, 2022, 9013);
COMMIT;


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
INSERT INTO `fact_enroll` VALUES (1, 15, 3001, 7001, 50001, 10, 101, 2021, 0);
INSERT INTO `fact_enroll` VALUES (2, 14, 3002, 7002, 50001, 10, 101, 2021, 0);
INSERT INTO `fact_enroll` VALUES (3, 13, 3001, 7001, 50002, 10, 101, 2021, 1);
INSERT INTO `fact_enroll` VALUES (4, 15, 3003, 7003, 50005, 20, 201, 2021, 0);
INSERT INTO `fact_enroll` VALUES (5, 14, 3004, 7004, 50006, 30, 301, 2022, 0);
INSERT INTO `fact_enroll` VALUES (6, 13, 3005, 7005, 50007, 40, 401, 2021, 0);
INSERT INTO `fact_enroll` VALUES (7, 15, 3006, 7006, 50008, 50, 501, 2021, 0);
INSERT INTO `fact_enroll` VALUES (8, 14, 3007, 7007, 50009, 60, 601, 2022, 1);
INSERT INTO `fact_enroll` VALUES (9, 13, 3008, 7008, 50010, 70, 701, 2021, 0);
INSERT INTO `fact_enroll` VALUES (10, 15, 3009, 7009, 50011, 80, 801, 2021, 0);
INSERT INTO `fact_enroll` VALUES (11, 14, 3010, 7010, 50012, 90, 901, 2021, 0);
INSERT INTO `fact_enroll` VALUES (12, 13, 3011, 7011, 50013, 100, 1001, 2021, 0);
INSERT INTO `fact_enroll` VALUES (13, 15, 3012, 7012, 50014, 110, 1101, 2022, 0);
INSERT INTO `fact_enroll` VALUES (14, 14, 3013, 7013, 50015, 120, 1201, 2021, 0);
INSERT INTO `fact_enroll` VALUES (15, 13, 3014, 7014, 50001, 10, 101, 2021, 1);
COMMIT;


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
INSERT INTO `sys_import_task` VALUES (1001, 'SCORE', 3, 'SUCCESS', 1000, 998, 2, '2026-03-02 10:00:00');
INSERT INTO `sys_import_task` VALUES (1002, 'SCORE', 4, 'SUCCESS', 1200, 1200, 0, '2026-03-03 10:00:00');
INSERT INTO `sys_import_task` VALUES (1003, 'ENROLL', 3, 'FAILED', 800, 750, 50, '2026-03-04 10:00:00');
INSERT INTO `sys_import_task` VALUES (1004, 'SCORE', 5, 'PENDING', 1500, 0, 0, '2026-03-05 10:00:00');
INSERT INTO `sys_import_task` VALUES (1005, 'ENROLL', 4, 'SUCCESS', 900, 900, 0, '2026-03-06 10:00:00');
INSERT INTO `sys_import_task` VALUES (1006, 'SCORE', 6, 'SUCCESS', 1000, 998, 2, '2026-03-07 10:00:00');
INSERT INTO `sys_import_task` VALUES (1007, 'SCORE', 7, 'SUCCESS', 1200, 1200, 0, '2026-03-08 10:00:00');
INSERT INTO `sys_import_task` VALUES (1008, 'ENROLL', 5, 'FAILED', 800, 750, 50, '2026-03-09 10:00:00');
INSERT INTO `sys_import_task` VALUES (1009, 'SCORE', 8, 'PENDING', 1500, 0, 0, '2026-03-10 10:00:00');
INSERT INTO `sys_import_task` VALUES (1010, 'ENROLL', 6, 'SUCCESS', 900, 900, 0, '2026-03-11 10:00:00');
INSERT INTO `sys_import_task` VALUES (1011, 'SCORE', 9, 'SUCCESS', 1000, 998, 2, '2026-03-12 10:00:00');
INSERT INTO `sys_import_task` VALUES (1012, 'SCORE', 10, 'SUCCESS', 1200, 1200, 0, '2026-03-13 10:00:00');
INSERT INTO `sys_import_task` VALUES (1013, 'ENROLL', 7, 'FAILED', 800, 750, 50, '2026-03-14 10:00:00');
INSERT INTO `sys_import_task` VALUES (1014, 'SCORE', 11, 'PENDING', 1500, 0, 0, '2026-03-15 10:00:00');
INSERT INTO `sys_import_task` VALUES (1015, 'ENROLL', 8, 'SUCCESS', 900, 900, 0, '2026-03-16 10:00:00');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
