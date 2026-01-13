$resDir = "app/src/main/res"
$sourceDirs = @("app/src/main/java", "app/src/main/kotlin", "app/src/main/res")

Write-Host "正在扫描资源文件..." -ForegroundColor Green
$allResources = @{}

Get-ChildItem -Path $resDir -Recurse -File -ErrorAction SilentlyContinue | Where-Object { -not $_.Name.StartsWith(".") } | ForEach-Object {
    $name = [IO.Path]::GetFileNameWithoutExtension($_.Name)
    $dir = (Split-Path $_.FullName -Leaf).Replace((Split-Path $_.FullName -Leaf), (Split-Path (Split-Path $_.FullName) -Leaf))

    $parentDir = (Split-Path (Split-Path $_.FullName) -Leaf)
    $type = ""

    if ($parentDir -match "^drawable") { $type = "drawable" }
    elseif ($parentDir -match "^layout") { $type = "layout" }
    elseif ($parentDir -match "^anim") { $type = "anim" }
    elseif ($parentDir -match "^color") { $type = "color" }
    elseif ($parentDir -match "^mipmap") { $type = "mipmap" }
    elseif ($parentDir -match "^menu") { $type = "menu" }
    elseif ($parentDir -match "^raw") { $type = "raw" }
    elseif ($parentDir -match "^values") { $type = "values" }
    else { $type = $parentDir }

    if (-not $allResources.ContainsKey($type)) {
        $allResources[$type] = @{}
    }
    $allResources[$type][$name] = $_.FullName
}

Write-Host "找到资源类型: $($allResources.Keys.Count)" -ForegroundColor Cyan

Write-Host "正在扫描源代码文件..." -ForegroundColor Green
$usedResources = @{}

$sourceDirs | ForEach-Object {
    $srcDir = $_
    if (Test-Path $srcDir) {
        Get-ChildItem -Path $srcDir -Recurse -File -Include *.kt,*.java,*.xml -ErrorAction SilentlyContinue | ForEach-Object {
            $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
            if ($content) {
                $regex1 = [regex]::new('R\.(drawable|layout|string|color|dimen|style|id|anim|menu|raw|mipmap|integer|bool|array|attr|font)\.([a-zA-Z0-9_]+)')
                $matches1 = $regex1.Matches($content)
                foreach ($m in $matches1) {
                    $t = $m.Groups[1].Value
                    $n = $m.Groups[2].Value
                    if (-not $usedResources.ContainsKey($t)) {
                        $usedResources[$t] = @{}
                    }
                    $usedResources[$t][$n] = $true
                }

                $regex2 = [regex]::new('@(drawable|layout|string|color|dimen|style|id|anim|menu|raw|mipmap|integer|bool|array|attr|font)/([a-zA-Z0-9_]+)')
                $matches2 = $regex2.Matches($content)
                foreach ($m in $matches2) {
                    $t = $m.Groups[1].Value
                    $n = $m.Groups[2].Value
                    if (-not $usedResources.ContainsKey($t)) {
                        $usedResources[$t] = @{}
                    }
                    $usedResources[$t][$n] = $true
                }
            }
        }
    }
}

Write-Host ""
Write-Host "分析结果:" -ForegroundColor Yellow
Write-Host ("=" * 60) -ForegroundColor Gray

$unusedFiles = [System.Collections.ArrayList]@()
$totalUnused = 0

$allResources.Keys | Sort-Object | ForEach-Object {
    $type = $_
    $resources = $allResources[$type]
    $usedKeys = if ($usedResources.ContainsKey($type)) { $usedResources[$type].Keys } else { @() }
    $unused = $resources.Keys | Where-Object { $_ -notin $usedKeys }

    if ($unused.Count -gt 0) {
        Write-Host ""
        Write-Host "$type : ($($unused.Count) 个)" -ForegroundColor Cyan
        $unused | Select-Object -First 20 | ForEach-Object { Write-Host "  - $_" }
        if ($unused.Count -gt 20) {
            Write-Host "  ... 还有 $($unused.Count - 20) 个"
        }
        $totalUnused += $unused.Count
        $unused | ForEach-Object {
            [void]$unusedFiles.Add($resources[$_])
        }
    }
}

Write-Host ""
Write-Host ("=" * 60) -ForegroundColor Gray
Write-Host "总计 $totalUnused 个未使用的资源" -ForegroundColor Red

$unusedFiles | Out-File -FilePath "unused_resources.txt" -Encoding UTF8
Write-Host "已保存到: unused_resources.txt" -ForegroundColor Green
