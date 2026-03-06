import re

filepath = 'd:/Code/smarteducationsystem_back/src/main/java/com/example/smarteducationsystem_back/mapper/OlapMapper.java'

with open(filepath, 'r', encoding='utf-8') as f:
    orig = f.read()

def replacer(match):
    block = match.group(0)
    # replace s.semester_id <= with s.semester_id &lt;=
    block_fixed = block.replace('s.semester_id <= #{endSemesterId}', 's.semester_id &lt;= #{endSemesterId}')
    # replace s.score <= with s.score &lt;= (just in case)
    block_fixed = block_fixed.replace('s.score <= #{pass}', 's.score &lt;= #{pass}')
    return block_fixed

# Replace ONLY inside @Select("<script>" blocks
new_text = re.sub(r'@Select\(\"<script>\".*?\"</script>\"\)', replacer, orig, flags=re.DOTALL)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(new_text)

print("Done replacing.")
