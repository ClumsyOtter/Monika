#!/bin/bash

echo "正在扫描资源文件..."
RES_DIR="app/src/main/res"

# 创建临时文件
TEMPDIR=$(mktemp -d)
ALL_RES="$TEMPDIR/all_res.txt"
USED_RES="$TEMPDIR/used_res.txt"

# 查找所有drawable资源（不包括values）
find "$RES_DIR/drawable"* -type f 2>/dev/null | while read f; do
    basename "$f" | sed 's/\.[^.]*$//'
done > "$ALL_RES"

# 查找所有layout资源
find "$RES_DIR/layout"* -type f 2>/dev/null | while read f; do
    basename "$f" | sed 's/\.[^.]*$//'
done >> "$ALL_RES"

# 查找所有anim资源  
find "$RES_DIR/anim"* -type f 2>/dev/null | while read f; do
    basename "$f" | sed 's/\.[^.]*$//'
done >> "$ALL_RES"

# 查找所有menu资源
find "$RES_DIR/menu"* -type f 2>/dev/null | while read f; do
    basename "$f" | sed 's/\.[^.]*$//'
done >> "$ALL_RES"

# 查找所有raw资源
find "$RES_DIR/raw"* -type f 2>/dev/null | while read f; do
    basename "$f" | sed 's/\.[^.]*$//'
done >> "$ALL_RES"

sort "$ALL_RES" | uniq > "$ALL_RES.sorted"

echo "正在扫描源代码文件..."

# 查找Java/Kotlin文件中的资源引用
find app/src/main/java app/src/main/kotlin -type f \( -name "*.kt" -o -name "*.java" \) 2>/dev/null | xargs grep -oh 'R\.\(drawable\|layout\|anim\|menu\|raw\)\.[a-zA-Z0-9_]*' 2>/dev/null | sed 's/.*\.\([a-zA-Z0-9_]*\)/\1/' > "$USED_RES"

# 查找XML文件中的资源引用
find app/src/main/res -type f -name "*.xml" 2>/dev/null | xargs grep -oh '@\(drawable\|layout\|anim\|menu\|raw\)/[a-zA-Z0-9_]*' 2>/dev/null | sed 's@.*/\([a-zA-Z0-9_]*\)@\1@' >> "$USED_RES"

sort "$USED_RES" | uniq > "$USED_RES.sorted"

echo ""
echo "分析结果:"
echo "=========================================="

# 找出未使用的资源
comm -23 "$ALL_RES.sorted" "$USED_RES.sorted" > "$TEMPDIR/unused.txt"
UNUSED_COUNT=$(wc -l < "$TEMPDIR/unused.txt")

echo "发现 $UNUSED_COUNT 个未使用的资源"

if [ $UNUSED_COUNT -gt 0 ]; then
    echo ""
    echo "未使用的资源 (前30个):"
    head -30 "$TEMPDIR/unused.txt" | while read name; do
        echo "  - $name"
    done
    
    if [ $UNUSED_COUNT -gt 30 ]; then
        echo "  ... 还有 $((UNUSED_COUNT - 30)) 个"
    fi
    
    # 查找对应的文件路径
    echo ""
    echo "文件路径 (将保存到 unused_resources.txt):"
    
    > unused_resources.txt
    while read name; do
        find "$RES_DIR" -name "${name}.*" 2>/dev/null >> unused_resources.txt
    done < "$TEMPDIR/unused.txt"
    
    FILE_COUNT=$(wc -l < unused_resources.txt)
    echo "共找到 $FILE_COUNT 个文件"
    echo "已保存到: unused_resources.txt"
fi

# 清理
rm -rf "$TEMPDIR"
