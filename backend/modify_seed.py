import sys

with open('prisma/seed.ts', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 1. change name to 求职招聘
for i, line in enumerate(lines):
    if "id: 'cat_job'" in line:
        lines[i+1] = lines[i+1].replace("'招聘求职'", "'求职招聘'")

# 2. find cat_job end (which is just before "7. 租车服务")
job_end_idx = -1
for i, line in enumerate(lines):
    if "7. " in line and "租车" in line:
        job_end_idx = i - 2
        break

# 3. find cat_part_time start and end
part_time_start = -1
part_time_end = -1
for i, line in enumerate(lines):
    if "8. " in line and "兼职" in line:
        part_time_start = i
        break

if part_time_start != -1:
    # Find the end of cat_part_time which is before "9. "
    for i in range(part_time_start + 1, len(lines)):
        if "9. " in lines[i]:
            part_time_end = i - 1
            break
            
    if part_time_end == -1:
        part_time_end = len(lines) - 1 # Fallback
            
    # Extract part_time children (lines inside children: [ ... ])
    children_start = -1
    for i in range(part_time_start, part_time_end):
        if "children: [" in lines[i]:
            children_start = i + 1
            break
            
    # Usually part time end block is:
    #     ]
    #   },
    children_end = part_time_end - 2
    
    if children_start != -1 and job_end_idx != -1:
        part_time_children = lines[children_start:children_end+1]
        
        # delete part time from original location
        del lines[part_time_start:part_time_end+1]
        
        # update job_end_idx because we didn't delete anything before it (part time is AFTER job)
        
        # We need to insert before `],` of cat_job
        # job_end_idx is the line with `    ],`
        # Let's ensure the line before job_end_idx has a comma
        prev_line_idx = job_end_idx - 1
        if not lines[prev_line_idx].strip().endswith(','):
            lines[prev_line_idx] = lines[prev_line_idx].rstrip('\n') + ',\n'
            
        lines[job_end_idx:job_end_idx] = part_time_children

with open('prisma/seed.ts', 'w', encoding='utf-8') as f:
    f.writelines(lines)

print("seed.ts updated successfully")
