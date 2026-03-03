const fs = require('fs');

const SQL_FILE = 'init.sql';

const SEMESTERS = [
  { id: 1, name: '2022-Fall', start_date: '2022-09-01', end_date: '2023-01-15' },
  { id: 2, name: '2023-Spring', start_date: '2023-02-25', end_date: '2023-07-10' },
  { id: 3, name: '2023-Fall', start_date: '2023-09-01', end_date: '2024-01-15' },
  { id: 4, name: '2024-Spring', start_date: '2024-02-25', end_date: '2024-07-10' },
  { id: 5, name: '2024-Fall', start_date: '2024-09-01', end_date: '2025-01-15' },
  { id: 6, name: '2025-Spring', start_date: '2025-02-25', end_date: '2025-07-10' },
  { id: 7, name: '2025-Fall', start_date: '2025-09-01', end_date: '2026-01-15' },
  { id: 8, name: '2026-Spring', start_date: '2026-02-25', end_date: '2026-07-10' }
];

const GRADES = [
  { id: 2022, name: '2022级' },
  { id: 2023, name: '2023级' },
  { id: 2024, name: '2024级' },
  { id: 2025, name: '2025级' }
];

const COLLEGES = [
  { id: 10, name: '计算机学院', code: '01' },
  { id: 20, name: '机械学院', code: '02' },
  { id: 30, name: '外语学院', code: '03' },
  { id: 60, name: '经济管理学院', code: '06' }
];

let sql = "-- 仅在本地测试使用，如数据库已存在会被删除重建\n";
sql += "DROP DATABASE IF EXISTS `smart_education`;\n";
sql += "CREATE DATABASE `smart_education` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;\n";
sql += "USE `smart_education`;\n\n";
sql += "SET NAMES utf8mb4;\n";
sql += "SET FOREIGN_KEY_CHECKS = 0;\n\n";

sql += "DROP TABLE IF EXISTS `sys_user`;\n";
sql += "CREATE TABLE `sys_user` (\n";
sql += "  `id` int(11) NOT NULL AUTO_INCREMENT,\n";
sql += "  `username` varchar(50) NOT NULL COMMENT '登录账户',\n";
sql += "  `password` varchar(100) NOT NULL COMMENT '密码 (演示暂存明文)',\n";
sql += "  `name` varchar(50) NOT NULL COMMENT '真实姓名',\n";
sql += "  `role_type` varchar(20) NOT NULL COMMENT 'SYS_ADMIN,SCHOOL_ADMIN,COLLEGE_ADMIN,TEACHER,STUDENT',\n";
sql += "  `college_id` int(11) DEFAULT NULL COMMENT '所属学院(仅院级管理员/老师/学生有)',\n";
sql += "  `student_id` int(11) DEFAULT NULL COMMENT '学生记录ID(仅学生有)',\n";
sql += "  `teacher_id` int(11) DEFAULT NULL COMMENT '教师记录ID(仅老师有)',\n";
sql += "  PRIMARY KEY (`id`),\n";
sql += "  UNIQUE KEY `uk_username` (`username`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统账号表';\n\n";

sql += "DROP TABLE IF EXISTS `sys_metric_config`;\n";
sql += "CREATE TABLE `sys_metric_config` (\n";
sql += "  `id` int(11) NOT NULL AUTO_INCREMENT,\n";
sql += "  `pass_score` double NOT NULL DEFAULT '60',\n";
sql += "  `excellent_score` double NOT NULL DEFAULT '85',\n";
sql += "  `score_bins` varchar(255) NOT NULL DEFAULT '[\"0-59\",\"60-69\",\"70-79\",\"80-84\",\"85-100\"]',\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标口径配置';\n\n";

sql += "BEGIN;\n";
sql += "INSERT INTO `sys_metric_config` VALUES (1, 60.0, 85.0, '[\"0-59\",\"60-69\",\"70-79\",\"80-84\",\"85-100\"]');\n";
sql += "COMMIT;\n\n";

sql += "BEGIN;\n";
sql += "INSERT INTO `sys_user` VALUES (1, 'admin', '123456', '系统管理员', 'SCHOOL_ADMIN', NULL, NULL, NULL);\n";

let currentUserId = 2;

function p(q) { sql += q + '\\n'; }
function e(s) { return "'" + s.replace(/'/g, "''") + "'"; }

let allTeachers = [];
let allCourses = [];
let allMajors = [];
let allClasses = [];
let allStudents = [];
let allEnrolls = [];
let allScores = [];

let majorIdCounter = 101;
for (const col of COLLEGES) {
  allMajors.push({ id: majorIdCounter, college_id: col.id, name: col.name.replace('学院', '') + '专业1', code: '01' });
  majorIdCounter++;
  allMajors.push({ id: majorIdCounter, college_id: col.id, name: col.name.replace('学院', '') + '专业2', code: '02' });
  majorIdCounter++;
}

let classIdCounter = 9001;
for (const grd of GRADES) {
  for (const maj of allMajors) {
    allClasses.push({ id: classIdCounter, major_id: maj.id, grade_id: grd.id, name: grd.name + maj.name + '1班' });
    classIdCounter++;
  }
}

let teacherIdCounter = 7001;
for (const col of COLLEGES) {
  for (let i = 1; i <= 5; i++) {
    let t = { id: teacherIdCounter, college_id: col.id, name: col.name.substring(0, 2) + '教师' + i, title: i % 2 === 0 ? '教授' : '讲师' };
    allTeachers.push(t);
    sql += "INSERT INTO `sys_user` VALUES (" + (currentUserId++) + ", '" + t.id + "', '123456', '" + t.name + "', 'TEACHER', " + col.id + ", NULL, " + t.id + ");\n";

    if (i === 1) {
      sql += "INSERT INTO `sys_user` VALUES (" + (currentUserId++) + ", '" + col.id + "_admin', '123456', '" + col.name + "管理员', 'COLLEGE_ADMIN', " + col.id + ", NULL, NULL);\n";
    }
    teacherIdCounter++;
  }
}
sql += "COMMIT;\n\n";

let courseIdCounter = 3001;
for (const col of COLLEGES) {
  allCourses.push({ id: courseIdCounter++, college_id: col.id, name: '大学英语', credit: 2.0, isGeneral: true });
  allCourses.push({ id: courseIdCounter++, college_id: col.id, name: '高等数学', credit: 4.0, isGeneral: true });

  let colMajors = allMajors.filter(m => m.college_id === col.id);
  for (const maj of colMajors) {
    for (let i = 1; i <= 3; i++) {
      allCourses.push({ id: courseIdCounter++, college_id: col.id, name: maj.name + '核心课' + i, credit: 3.0, isGeneral: false, typeMajorId: maj.id });
    }
  }
}

let studentIdCounter = 50001;
let seqCounters = {};
sql += "BEGIN;\n";
for (const cls of allClasses) {
  let m = allMajors.find(x => x.id === cls.major_id);
  let c = COLLEGES.find(x => x.id === m.college_id);
  let g = GRADES.find(x => x.id === cls.grade_id);

  let prefix = String(g.id) + c.code + m.code;
  if (!seqCounters[prefix]) seqCounters[prefix] = 1;

  for (let i = 0; i < 12; i++) {
    let seqcode = String(seqCounters[prefix]++).padStart(4, '0');
    let sno = prefix + seqcode;
    let sName = m.name.substring(0, 2) + '生' + (seqCounters[prefix] - 1);

    let stu = { id: studentIdCounter++, sno: sno, name: sName, college_id: c.id, major_id: m.id, grade_id: g.id, class_id: cls.id };
    allStudents.push(stu);

    sql += "INSERT INTO `sys_user` VALUES (" + (currentUserId++) + ", '" + stu.sno + "', '123456', '" + stu.name + "', 'STUDENT', " + c.id + ", " + stu.id + ", NULL);\n";
  }
}
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_semester`;\n";
sql += "CREATE TABLE `dim_semester` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  `start_date` date DEFAULT NULL,\n";
sql += "  `end_date` date DEFAULT NULL,\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
SEMESTERS.forEach(s => sql += "INSERT INTO `dim_semester` VALUES (" + s.id + ", " + e(s.name) + ", " + e(s.start_date) + ", " + e(s.end_date) + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_college`;\n";
sql += "CREATE TABLE `dim_college` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
COLLEGES.forEach(c => sql += "INSERT INTO `dim_college` VALUES (" + c.id + ", " + e(c.name) + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_major`;\n";
sql += "CREATE TABLE `dim_major` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `college_id` int(11) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
allMajors.forEach(m => sql += "INSERT INTO `dim_major` VALUES (" + m.id + ", " + m.college_id + ", " + e(m.name) + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_grade`;\n";
sql += "CREATE TABLE `dim_grade` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
GRADES.forEach(g => sql += "INSERT INTO `dim_grade` VALUES (" + g.id + ", " + e(g.name) + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_class`;\n";
sql += "CREATE TABLE `dim_class` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `grade_id` int(11) NOT NULL,\n";
sql += "  `major_id` int(11) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
allClasses.forEach(c => sql += "INSERT INTO `dim_class` VALUES (" + c.id + ", " + c.grade_id + ", " + c.major_id + ", " + e(c.name) + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_teacher`;\n";
sql += "CREATE TABLE `dim_teacher` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `college_id` int(11) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  `title` varchar(50) DEFAULT NULL,\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
allTeachers.forEach(t => sql += "INSERT INTO `dim_teacher` VALUES (" + t.id + ", " + t.college_id + ", " + e(t.name) + ", " + e(t.title) + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_course`;\n";
sql += "CREATE TABLE `dim_course` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `college_id` int(11) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  `credit` double NOT NULL,\n";
sql += "  PRIMARY KEY (`id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
allCourses.forEach(c => sql += "INSERT INTO `dim_course` VALUES (" + c.id + ", " + c.college_id + ", " + e(c.name) + ", " + c.credit + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `dim_student`;\n";
sql += "CREATE TABLE `dim_student` (\n";
sql += "  `id` int(11) NOT NULL,\n";
sql += "  `student_no` varchar(20) NOT NULL,\n";
sql += "  `name` varchar(50) NOT NULL,\n";
sql += "  `college_id` int(11) NOT NULL,\n";
sql += "  `major_id` int(11) NOT NULL,\n";
sql += "  `grade_id` int(11) NOT NULL,\n";
sql += "  `class_id` int(11) NOT NULL,\n";
sql += "  PRIMARY KEY (`id`),\n";
sql += "  UNIQUE KEY `uk_student_no` (`student_no`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n";
sql += "BEGIN;\n";
allStudents.forEach(s => sql += "INSERT INTO `dim_student` VALUES (" + s.id + ", " + e(s.sno) + ", " + e(s.name) + ", " + s.college_id + ", " + s.major_id + ", " + s.grade_id + ", " + s.class_id + ");\n");
sql += "COMMIT;\n\n";

sql += "DROP TABLE IF EXISTS `fact_enroll`;\n";
sql += "CREATE TABLE `fact_enroll` (\n";
sql += "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n";
sql += "  `semester_id` int(11) NOT NULL,\n";
sql += "  `student_id` int(11) NOT NULL,\n";
sql += "  `course_id` int(11) NOT NULL,\n";
sql += "  `teacher_id` int(11) NOT NULL,\n";
sql += "  `is_drop` tinyint(1) NOT NULL DEFAULT '0',\n";
sql += "  `college_id` int(11) NOT NULL,\n";
sql += "  `major_id` int(11) NOT NULL,\n";
sql += "  `grade_id` int(11) NOT NULL,\n";
sql += "  `class_id` int(11) NOT NULL,\n";
sql += "  PRIMARY KEY (`id`),\n";
sql += "  KEY `idx_course_teacher` (`course_id`,`teacher_id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n";

sql += "DROP TABLE IF EXISTS `fact_score`;\n";
sql += "CREATE TABLE `fact_score` (\n";
sql += "  `id` bigint(20) NOT NULL AUTO_INCREMENT,\n";
sql += "  `semester_id` int(11) NOT NULL,\n";
sql += "  `student_id` int(11) NOT NULL,\n";
sql += "  `course_id` int(11) NOT NULL,\n";
sql += "  `teacher_id` int(11) NOT NULL,\n";
sql += "  `score` double DEFAULT NULL,\n";
sql += "  `is_absent` tinyint(1) NOT NULL DEFAULT '0',\n";
sql += "  `college_id` int(11) NOT NULL,\n";
sql += "  `major_id` int(11) NOT NULL,\n";
sql += "  `grade_id` int(11) NOT NULL,\n";
sql += "  `class_id` int(11) NOT NULL,\n";
sql += "  PRIMARY KEY (`id`),\n";
sql += "  KEY `idx_student` (`student_id`),\n";
sql += "  KEY `idx_olap_filter` (`semester_id`,`college_id`,`major_id`,`grade_id`)\n";
sql += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n";

const GRADE_START_MAP = { 2022: 1, 2023: 3, 2024: 5, 2025: 7 };
let factIdCounter = 1;

function randScore() {
  let u = 0, v = 0;
  while (u === 0) u = Math.random();
  while (v === 0) v = Math.random();
  let num = Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
  num = num / 10.0 + 0.5;
  if (num > 1 || num < 0) num = randScore();
  let s = Math.round((num * 50 + 40) * 10) / 10;
  if (s > 100) s = 100;
  if (s < 0) s = 0;
  return s;
}

const bSize = 500;
let eb = [], sb = [];

function flush() {
  if (eb.length > 0) {
    sql += "BEGIN;\n";
    sql += "INSERT INTO `fact_enroll` VALUES " + eb.join(',\n') + ";\n";
    sql += "COMMIT;\n\n";
    eb = [];
  }
  if (sb.length > 0) {
    sql += "BEGIN;\n";
    sql += "INSERT INTO `fact_score` VALUES " + sb.join(',\n') + ";\n";
    sql += "COMMIT;\n\n";
    sb = [];
  }
}

for (const stu of allStudents) {
  let start_sem = GRADE_START_MAP[stu.grade_id];
  let end_sem = Math.min(8, start_sem + 7);

  let cList = allCourses.filter(c => c.college_id === stu.college_id && (c.isGeneral || c.typeMajorId === stu.major_id));
  let studentCourses = cList.slice(0, 5);
  let colTeachers = allTeachers.filter(t => t.college_id === stu.college_id);

  for (let sem = start_sem; sem <= end_sem; sem++) {
    for (const c of studentCourses) {
      let t = colTeachers[c.id % colTeachers.length];
      let score = randScore();
      let isAbsent = Math.random() < 0.01 ? 1 : 0;
      let fScore = isAbsent ? 'NULL' : score;

      eb.push("(" + factIdCounter + ", " + sem + ", " + stu.id + ", " + c.id + ", " + t.id + ", 0, " + stu.college_id + ", " + stu.major_id + ", " + stu.grade_id + ", " + stu.class_id + ")");
      sb.push("(" + factIdCounter + ", " + sem + ", " + stu.id + ", " + c.id + ", " + t.id + ", " + fScore + ", " + isAbsent + ", " + stu.college_id + ", " + stu.major_id + ", " + stu.grade_id + ", " + stu.class_id + ")");

      factIdCounter++;
      if (eb.length >= bSize) flush();
    }
  }
}
flush();

fs.writeFileSync(SQL_FILE, sql, 'utf8');
console.log('Successfully generated ' + factIdCounter + ' records to init.sql!');
