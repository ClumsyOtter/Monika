#!/bin/bash

echo "正在扫描资源文件..."

RES_DIR="app/src/main/res"
PROJECT_ROOT=$(pwd)

# 临时文件
USED_RESOURCES=$(mktemp)
ALL_RESOURCES=$(mktemp)

# 查找所有资源文件
find "$RES_DIR" -type f ! -name '.*' | while read file; do
    # 获取相对路径
    rel_path="${file#$RES_DIR/}"
    dir=$(echo "$rel_path" | cut -d'/' -f1)
    filename=$(basename "$file")
    name="${filename%.*}"
    
    # 确定资源类型
    case "$dir" in
        drawable-*|drawable) type="drawable" ;;
        layout-*|layout) type="layout" ;;
        values-*|values) type="values" ;;
        anim-*|anim) type="anim" ;;
        color-*|color) type="color" ;;
        mipmap-*|mipmap) type="mipmap" ;;
        menu-*|menu) type="menu" ;;
        raw-*|raw) type="raw" ;;
        *) type="$dir" ;;
    esac
    
    echo "$type:$name" >> "$ALL_RESOURCES"
done

# 扫描源代码查找资源引用
echo "正在扫描源代码文件..."

find app/src/main -type f \( -name "*.kt" -o -name "*.java" -o -name "*.xml" \) -exec grep -ohE 'R\.(drawable|layout|string|color|dimen|style|id|anim|menu|raw|mipmap|integer|bool|array|attr|plurals|font)\.[a-zA-Z0-9_]+' {} \; 2>/dev/null | \
    sed 's/R\.\([^.]*\)\.\(.*\)/\1:\2/' >> "$USED_RESOURCES"

find app/src/main -type f -name "*.xml" -exec grep -ohE '@[a-z]+/[a-zA-Z0-9_]+' {} \; 2>/dev/null | \
    sed 's/@\([a-z]*\)\/\(.*\)/\1:\2/' >> "$USED_RESOURCES"

# 找出未使用的资源
echo ""
echo "分析结果:"
echo "=================================="

sort "$ALL_RESOURCES" | uniq > "$ALL_RESOURCES.sorted"
sort "$USED_RESOURCES" | uniq > "$USED_RESOURCES.sorted"

# 找出差异
comm -23 "$ALL_RESOURCES.sorted" "$USED_RESOURCES.sorted" | while read line; do
    type="${line%:*}"
    name="${line#*:}"
    
    # 查找实际文件
    find "$RES_DIR" -type f -name "${name}.*" | while read file; do
        file_rel="${file#$PROJECT_ROOT/}"
        # 检查文件类型是否匹配
        file_dir=$(dirname "$file")
        file_dirname=$(basename "$file_dir")
        
        case "$file_dirname" in
            ${type}-*|${type})
                echo "$file_rel"
                ;;
        esac
    done
done

# 清理
rm -f "$ALL_RESOURCES" "$USED_RESOURCES" "$ALL_RESOURCES.sorted" "$USED_RESOURCES.sorted"

