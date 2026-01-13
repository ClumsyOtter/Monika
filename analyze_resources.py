import os
import re
from pathlib import Path
from collections import defaultdict

# 项目根目录
PROJECT_ROOT = r"E:\AndroidProjects\Monika"
RES_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "res")
SOURCE_DIRS = [
    os.path.join(PROJECT_ROOT, "app", "src", "main", "java"),
    os.path.join(PROJECT_ROOT, "app", "src", "main", "kotlin"),
    RES_DIR
]

# 存储资源信息
resources = defaultdict(set)
# 存储被引用的资源
used_resources = defaultdict(set)

def find_all_resources():
    """查找所有资源文件"""
    print("正在扫描资源文件...")
    
    for root, dirs, files in os.walk(RES_DIR):
        for file in files:
            # 跳过以.开头的文件
            if file.startswith('.'):
                continue
                
            file_path = os.path.join(root, file)
            rel_path = os.path.relpath(file_path, RES_DIR)
            
            # 确定资源类型和名称
            parts = rel_path.split(os.sep)
            if len(parts) >= 2:
                resource_dir = parts[0]  # drawable, layout, etc.
                
                # 提取资源名称（去掉密度限定符等）
                resource_name = parts[-1]
                
                # 去掉扩展名和密度限定符
                name_without_ext = os.path.splitext(resource_name)[0]
                
                # 处理密度限定符目录 (如 drawable-xhdpi)
                if resource_dir.startswith('drawable-') or resource_dir.startswith('mipmap-'):
                    base_type = resource_dir.split('-')[0]
                else:
                    base_type = resource_dir
                
                resources[base_type].add(name_without_ext)
    
    print(f"找到资源类型: {list(resources.keys())}")
    for res_type, files in resources.items():
        print(f"  {res_type}: {len(files)} 个文件")

def scan_source_files():
    """扫描源代码文件查找资源引用"""
    print("\n正在扫描源代码文件...")
    
    # 资源引用的正则表达式模式
    patterns = [
        r'R\.(\w+)\.(\w+)',  # R.type.name
        r'@(\w+)/(\w+)',      # @type/name in XML
    ]
    
    source_count = 0
    for source_dir in SOURCE_DIRS:
        if not os.path.exists(source_dir):
            continue
            
        for root, dirs, files in os.walk(source_dir):
            for file in files:
                if file.endswith(('.kt', '.java', '.xml')):
                    source_count += 1
                    file_path = os.path.join(root, file)
                    
                    try:
                        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                            content = f.read()
                            
                            for pattern in patterns:
                                matches = re.finditer(pattern, content)
                                for match in matches:
                                    res_type = match.group(1)
                                    res_name = match.group(2)
                                    
                                    # 过滤掉非资源类型
                                    if res_type in ['drawable', 'layout', 'string', 'color', 'dimen', 
                                                   'style', 'id', 'anim', 'menu', 'raw', 'mipmap',
                                                   'integer', 'bool', 'array', 'attr', 'plurals', 'font']:
                                        used_resources[res_type].add(res_name)
                    except Exception as e:
                        print(f"读取文件出错 {file_path}: {e}")
    
    print(f"扫描了 {source_count} 个源代码文件")

def find_unused_resources():
    """找出未使用的资源"""
    print("\n正在分析未使用的资源...")
    
    unused = {}
    total_unused = 0
    
    for res_type in sorted(resources.keys()):
        all_files = resources[res_type]
        used_files = used_resources.get(res_type, set())
        unused_files = all_files - used_files
        
        if unused_files:
            unused[res_type] = sorted(unused_files)
            total_unused += len(unused_files)
    
    return unused, total_unused

def find_unused_files_with_paths(unless_resources):
    """找到未使用资源的实际文件路径"""
    unused_file_paths = []
    
    for root, dirs, files in os.walk(RES_DIR):
        for file in files:
            if file.startswith('.'):
                continue
                
            file_path = os.path.join(root, file)
            rel_path = os.path.relpath(file_path, RES_DIR)
            
            # 提取资源名称
            name_without_ext = os.path.splitext(file)[0]
            
            # 检查是否在未使用资源列表中
            for res_type, unused_names in unless_resources.items():
                if name_without_ext in unused_names:
                    # 检查类型匹配
                    parts = rel_path.split(os.sep)
                    if len(parts) >= 2:
                        resource_dir = parts[0]
                        
                        # 处理密度限定符
                        if resource_dir.startswith(res_type + '-') or resource_dir == res_type:
                            unused_file_paths.append(file_path)
    
    return unused_file_paths

if __name__ == "__main__":
    find_all_resources()
    scan_source_files()
    
    unused, total = find_unused_resources()
    
    print(f"\n" + "="*60)
    print(f"分析结果:")
    print(f"="*60)
    
    if unused:
        print(f"\n发现 {total} 个未使用的资源:")
        print("-"*60)
        
        for res_type, files in unused.items():
            print(f"\n{res_type}: ({len(files)} 个)")
            for f in files[:20]:  # 只显示前20个
                print(f"  - {f}")
            if len(files) > 20:
                print(f"  ... 还有 {len(files) - 20} 个")
        
        # 找到实际的文件路径
        print("\n" + "="*60)
        unused_files = find_unused_files_with_paths(unused)
        print(f"\n总共 {len(unused_files)} 个文件将被删除")
        
        # 保存文件列表
        with open(os.path.join(PROJECT_ROOT, "unused_resources.txt"), "w", encoding="utf-8") as f:
            for file_path in sorted(unused_files):
                f.write(file_path + "\n")
        
        print(f"\n未使用资源列表已保存到: {os.path.join(PROJECT_ROOT, 'unused_resources.txt')}")
    else:
        print("\n未发现未使用的资源！")
