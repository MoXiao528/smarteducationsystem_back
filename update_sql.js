const fs = require('fs');

try {
    let txt = fs.readFileSync('init.sql', 'utf8');

    // replace student dim table inserts
    // INSERT INTO `dim_student` VALUES (50001, 'S20210001', '张三', 10, 101, 2021, 9001);
    txt = txt.replace(/INSERT INTO `dim_student` VALUES \((\d+), '([^']+)', '([^']+)', (\d+), (\d+), (\d+), (\d+)\);/g, (match, id, oldSno, name, college, major, grade, cls) => {
        let year = grade;
        let c_code = String(Math.floor(parseInt(college) / 10)).padStart(2, '0');
        let m_code = String(parseInt(major) % 100).padStart(2, '0');
        let seq = parseInt(id) % 50000;
        let seq_code = String(seq).padStart(4, '0');
        let newSno = year + c_code + m_code + seq_code;
        return "INSERT INTO `dim_student` VALUES (" + id + ", '" + newSno + "', '" + name + "', " + college + ", " + major + ", " + grade + ", " + cls + ");";
    });

    // update teacher users usernames to their teacher_id
    // INSERT INTO `sys_user` VALUES (3, 'teacher1', '123456', '教师1', 'TEACHER', 10, NULL, 7001);
    txt = txt.replace(/INSERT INTO `sys_user` VALUES \((\d+), 'teacher\d+', '123456', '([^']+)', 'TEACHER', (\d+), NULL, (\d+)\);/g, (match, id, name, college, t_id) => {
        return "INSERT INTO `sys_user` VALUES (" + id + ", '" + t_id + "', '123456', '" + name + "', 'TEACHER', " + college + ", NULL, " + t_id + ");";
    });

    // mapping student no
    let snoMap = {};
    for (const match of txt.matchAll(/INSERT INTO `dim_student` VALUES \((\d+), '([^']+)'/g)) {
        snoMap[match[1]] = match[2];
    }

    // update student users username
    // INSERT INTO `sys_user` VALUES (6, 'student1', '123456', '学生1', 'STUDENT', 10, 50001, NULL);
    txt = txt.replace(/INSERT INTO `sys_user` VALUES \((\d+), 'student\d+', '123456', '([^']+)', 'STUDENT', (\d+), (\d+), NULL\);/g, (match, id, name, college, s_id) => {
        let newSno = snoMap[s_id];
        return "INSERT INTO `sys_user` VALUES (" + id + ", '" + newSno + "', '123456', '" + name + "', 'STUDENT', " + college + ", " + s_id + ", NULL);";
    });

    fs.writeFileSync('init.sql', txt, 'utf8');
    console.log('Update init.sql done!');
} catch (e) {
    console.error(e);
}
