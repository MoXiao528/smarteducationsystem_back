const fs = require('fs');

function randomChoice(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min) + min);
}

let sql = `-- 仅在本地测试使用，如数据库已存在会被删除重建
DROP DATABASE IF EXISTS \`smart_education\`;
CREATE DATABASE \`smart_education\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE \`smart_education\`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 系统配置表与权限表
-- ----------------------------
DROP TABLE IF EXISTS \`sys_user\`;
CREATE TABLE \`sys_user\` (
  \`id\` int(11) NOT NULL AUTO_INCREMENT,
  \`username\` varchar(50) NOT NULL COMMENT '登录账户',
  \`password\` varchar(100) NOT NULL COMMENT '密码 (演示暂存明文)',
  \`name\` varchar(50) NOT NULL COMMENT '真实姓名',
  \`role_type\` varchar(20) NOT NULL COMMENT 'SYS_ADMIN,SCHOOL_ADMIN,COLLEGE_ADMIN,TEACHER,STUDENT',
  \`college_id\` int(11) DEFAULT NULL COMMENT '所属学院(仅院级管理员/老师/学生有)',
  \`student_id\` int(11) DEFAULT NULL COMMENT '学生记录ID(仅学生有)',
  \`teacher_id\` int(11) DEFAULT NULL COMMENT '教师记录ID(仅老师有)',
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`uk_username\` (\`username\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统账号表';

BEGIN;
INSERT INTO \`sys_user\` VALUES (1, 'admin', '123456', '系统管理员', 'SCHOOL_ADMIN', NULL, NULL, NULL);
INSERT INTO \`sys_user\` VALUES (2, 'cs_admin', '123456', '计科院管', 'COLLEGE_ADMIN', 10, NULL, NULL);
`;

let teacherStart = 7001;
for (let i = 1; i <= 15; i++) {
    let t_id = teacherStart + i - 1;
    sql += `INSERT INTO \`sys_user\` VALUES (${2 + i}, '${t_id}', '123456', '教师${i}', 'TEACHER', 10, NULL, ${t_id});\n`;
}

// Student users will be appended later
let studentUserSql = "";
let userId = 18;

sql += `COMMIT;

DROP TABLE IF EXISTS \`sys_metric_config\`;
CREATE TABLE \`sys_metric_config\` (
  \`id\` int(11) NOT NULL AUTO_INCREMENT,
  \`pass_score\` double NOT NULL DEFAULT '60',
  \`excellent_score\` double NOT NULL DEFAULT '85',
  \`score_bins\` varchar(255) NOT NULL DEFAULT '["0-59","60-69","70-79","80-84","85-100"]',
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标口径配置';

BEGIN;
INSERT INTO \`sys_metric_config\` VALUES (1, 60.0, 85.0, '["0-59","60-69","70-79","80-84","85-100"]');
COMMIT;

-- ----------------------------
-- 2. 维度表
-- ----------------------------
DROP TABLE IF EXISTS \`dim_semester\`;
CREATE TABLE \`dim_semester\` (
  \`id\` int(11) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  \`start_date\` date DEFAULT NULL,
  \`end_date\` date DEFAULT NULL,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期维度表';

BEGIN;
`;

for (let i = 2009; i < 2024; i++) {
    sql += `INSERT INTO \`dim_semester\` VALUES (${i - 2008}, '${i}-Fall', '${i}-09-01', '${i + 1}-01-15');\n`;
    sql += `INSERT INTO \`dim_semester\` VALUES (${i - 2008 + 15}, '${i + 1}-Spring', '${i + 1}-02-25', '${i + 1}-07-10');\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`dim_college\`;
CREATE TABLE \`dim_college\` (
  \`id\` int(11) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院维度表';

BEGIN;
`;

const colleges = ['计算机学院', '机械学院', '外语学院', '理学院', '化工学院', '经济管理学院', '法学院', '艺术学院', '体育学院', '电子信息学院', '材料学院', '环境学院', '生命科学学院', '医学院', '航空航天学院', '建筑学院'];
const college_ids = [];
for (let i = 0; i < colleges.length; i++) {
    let c_id = (i + 1) * 10;
    college_ids.push(c_id);
    sql += `INSERT INTO \`dim_college\` VALUES (${c_id}, '${colleges[i]}');\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`dim_major\`;
CREATE TABLE \`dim_major\` (
  \`id\` int(11) NOT NULL,
  \`college_id\` int(11) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业维度表';

BEGIN;
`;

const major_ids = [];
for (let i = 0; i < college_ids.length; i++) {
    let c_id = college_ids[i];
    let m_id1 = (i + 1) * 100 + 1;
    let m_id2 = (i + 1) * 100 + 2;
    major_ids.push(m_id1, m_id2);
    let pname = colleges[i].substring(0, 2);
    sql += `INSERT INTO \`dim_major\` VALUES (${m_id1}, ${c_id}, '${pname}专业1');\n`;
    sql += `INSERT INTO \`dim_major\` VALUES (${m_id2}, ${c_id}, '${pname}专业2');\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`dim_grade\`;
CREATE TABLE \`dim_grade\` (
  \`id\` int(11) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级维度表';

BEGIN;
`;

const grade_ids = [];
for (let i = 2005; i < 2025; i++) {
    grade_ids.push(i);
    sql += `INSERT INTO \`dim_grade\` VALUES (${i}, '${i}级');\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`dim_class\`;
CREATE TABLE \`dim_class\` (
  \`id\` int(11) NOT NULL,
  \`grade_id\` int(11) NOT NULL,
  \`major_id\` int(11) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级维度表';

BEGIN;
`;

const class_ids = [];
for (let i = 1; i <= 20; i++) {
    let c_id = 9000 + i;
    let grade = randomChoice(grade_ids);
    let major = randomChoice(major_ids);
    class_ids.push(c_id);
    sql += `INSERT INTO \`dim_class\` VALUES (${c_id}, ${grade}, ${major}, '班级${i}');\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`dim_teacher\`;
CREATE TABLE \`dim_teacher\` (
  \`id\` int(11) NOT NULL,
  \`college_id\` int(11) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  \`title\` varchar(50) DEFAULT NULL,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师维度表';

BEGIN;
`;

const teacher_ids = [];
for (let i = 1; i <= 20; i++) {
    let t_id = 7000 + i;
    teacher_ids.push(t_id);
    let college = randomChoice(college_ids);
    let title = randomChoice(['教授', '副教授', '讲师', '助教']);
    sql += `INSERT INTO \`dim_teacher\` VALUES (${t_id}, ${college}, '教师${i}', '${title}');\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`dim_course\`;
CREATE TABLE \`dim_course\` (
  \`id\` int(11) NOT NULL,
  \`college_id\` int(11) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  \`credit\` double NOT NULL,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程维度表';

BEGIN;
`;

const course_ids = [];
const credits = [2.0, 3.0, 3.5, 4.0, 5.0];
for (let i = 1; i <= 20; i++) {
    let c_id = 3000 + i;
    course_ids.push(c_id);
    let college = randomChoice(college_ids);
    let credit = randomChoice(credits);
    sql += `INSERT INTO \`dim_course\` VALUES (${c_id}, ${college}, '课程${i}', ${credit});\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`dim_student\`;
CREATE TABLE \`dim_student\` (
  \`id\` int(11) NOT NULL,
  \`student_no\` varchar(20) NOT NULL,
  \`name\` varchar(50) NOT NULL,
  \`college_id\` int(11) NOT NULL,
  \`major_id\` int(11) NOT NULL,
  \`grade_id\` int(11) NOT NULL,
  \`class_id\` int(11) NOT NULL,
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`uk_student_no\` (\`student_no\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生维度表';

BEGIN;
`;

const student_ids = [];
const students_info = {};
for (let i = 1; i <= 30; i++) {
    let s_id = 50000 + i;
    student_ids.push(s_id);
    let year = randomInt(2020, 2024);
    let college = randomChoice(college_ids);
    let major = randomChoice(major_ids.filter(m => String(m).startsWith(String(college).substring(0, String(college).length - 1))));
    let grade = year;
    let cls = randomChoice(class_ids);
    
    // Format: year(4) + college(2) + major(2) + class_seq(4)
    let c_code = String(Math.floor(college / 10)).padStart(2, '0');
    let m_code = String(major % 100).padStart(2, '0');
    let seq_code = String(i).padStart(4, '0');
    let s_no = \`\${year}\${c_code}\${m_code}\${seq_code}\`;
    
    students_info[s_id] = { college, major, grade, class: cls };
    sql += `INSERT INTO \`dim_student\` VALUES (${s_id}, '${s_no}', '学生${i}', ${college}, ${major}, ${grade}, ${cls});\n`;
    studentUserSql += `INSERT INTO \`sys_user\` VALUES (${userId}, '${s_no}', '123456', '学生${i}', 'STUDENT', ${college}, ${s_id}, NULL);\n`;
    userId++;
}

sql += `COMMIT;

BEGIN;\n${studentUserSql}COMMIT;

-- ----------------------------
-- 3. OLTP 事实表
-- ----------------------------
DROP TABLE IF EXISTS \`fact_score\`;
CREATE TABLE \`fact_score\` (
  \`id\` bigint(20) NOT NULL AUTO_INCREMENT,
  \`semester_id\` int(11) NOT NULL,
  \`student_id\` int(11) NOT NULL,
  \`course_id\` int(11) NOT NULL,
  \`teacher_id\` int(11) NOT NULL,
  \`score\` double DEFAULT NULL COMMENT '成绩',
  \`is_absent\` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否缺考，1缺考 0否',
  \`college_id\` int(11) NOT NULL,
  \`major_id\` int(11) NOT NULL,
  \`grade_id\` int(11) NOT NULL,
  \`class_id\` int(11) NOT NULL,
  PRIMARY KEY (\`id\`),
  KEY \`idx_student\` (\`student_id\`),
  KEY \`idx_course\` (\`course_id\`),
  KEY \`idx_semester_college\` (\`semester_id\`,\`college_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩明细事实表';

BEGIN;
`;

for (let i = 1; i <= 50; i++) {
    let semester = randomInt(1, 16);
    let student = randomChoice(student_ids);
    let course = randomChoice(course_ids);
    let teacher = randomChoice(teacher_ids);
    let is_absent = Math.random() < 0.05 ? 1 : 0;
    let score = is_absent ? "NULL" : (Math.random() * 60 + 40).toFixed(1);
    let info = students_info[student];
    sql += `INSERT INTO \`fact_score\` VALUES (${i}, ${semester}, ${student}, ${course}, ${teacher}, ${score}, ${is_absent}, ${info.college}, ${info.major}, ${info.grade}, ${info.class});\n`;
}

sql += `COMMIT;

DROP TABLE IF EXISTS \`fact_enroll\`;
CREATE TABLE \`fact_enroll\` (
  \`id\` bigint(20) NOT NULL AUTO_INCREMENT,
  \`semester_id\` int(11) NOT NULL,
  \`course_id\` int(11) NOT NULL,
  \`teacher_id\` int(11) NOT NULL,
  \`student_id\` int(11) NOT NULL,
  \`college_id\` int(11) NOT NULL,
  \`major_id\` int(11) NOT NULL,
  \`grade_id\` int(11) NOT NULL,
  \`is_drop\` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否退课 1是 0否',
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课明细表';

BEGIN;
`;

for (let i = 1; i <= 30; i++) {
    let semester = randomInt(1, 16);
    let student = randomChoice(student_ids);
    let course = randomChoice(course_ids);
    let teacher = randomChoice(teacher_ids);
    let is_drop = Math.random() < 0.1 ? 1 : 0;
    let info = students_info[student];
    sql += `INSERT INTO \`fact_enroll\` VALUES (${i}, ${semester}, ${course}, ${teacher}, ${student}, ${info.college}, ${info.major}, ${info.grade}, ${is_drop});\n`;
}

sql += `COMMIT;

-- ----------------------------
-- 4. 导入任务表
-- ----------------------------
DROP TABLE IF EXISTS \`sys_import_task\`;
CREATE TABLE \`sys_import_task\` (
  \`task_id\` bigint(20) NOT NULL AUTO_INCREMENT,
  \`type\` varchar(20) NOT NULL COMMENT 'SCORE, ENROLL',
  \`semester_id\` int(11) DEFAULT NULL,
  \`status\` varchar(20) NOT NULL COMMENT 'PENDING, SUCCESS, FAILED',
  \`total_rows\` int(11) NOT NULL DEFAULT '0',
  \`success_rows\` int(11) NOT NULL DEFAULT '0',
  \`failed_rows\` int(11) NOT NULL DEFAULT '0',
  \`created_at\` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (\`task_id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导入任务记录';

BEGIN;
`;

const types = ['SCORE', 'ENROLL'];
const statuses = ['SUCCESS', 'FAILED', 'PENDING'];
for (let i = 1001; i <= 1015; i++) {
    let type_str = randomChoice(types);
    let status = randomChoice(statuses);
    sql += `INSERT INTO \`sys_import_task\` VALUES (${i}, '${type_str}', 3, '${status}', 1000, 998, 2, '2026-03-02 10:00:00');\n`;
}

sql += `COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
`;

fs.writeFileSync('init.sql', sql, 'utf-8');
console.log("Successfully generated init.sql");
