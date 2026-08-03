$ErrorActionPreference = 'Continue'
$base = 'd:\Android\Projects\MyWeiXinclient\push_temp'
$client = Join-Path $base 'client'
$server = Join-Path $base 'server'

# Create directories
New-Item -ItemType Directory -Path (Join-Path $client 'java') -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $client 'res') -Force | Out-Null
New-Item -ItemType Directory -Path $server -Force | Out-Null

# 1. Copy Java files
$javaSrc = 'd:\Android\Projects\MyWeiXinclient\app\src\main\java\com\example\myweixin_client\src-client\java'
Get-ChildItem -Path $javaSrc -Filter '*.java' | ForEach-Object {
    Copy-Item $_.FullName -Destination (Join-Path $client 'java') -Force
}
Write-Host "Java files copied: $(Get-ChildItem -Path $javaSrc -Filter '*.java').Count"

# 2. Copy res directory
$resSrc = 'd:\Android\Projects\MyWeiXinclient\app\src\main\res'
Copy-Item -Path $resSrc -Destination (Join-Path $client 'res') -Recurse -Force
Write-Host "res directory copied"

# 3. Copy AndroidManifest.xml
Copy-Item 'd:\Android\Projects\MyWeiXinclient\app\src\main\AndroidManifest.xml' -Destination $client -Force
Write-Host "AndroidManifest.xml copied"

# 4. Copy build files
Copy-Item 'd:\Android\Projects\MyWeiXinclient\app\build.gradle' -Destination (Join-Path $client 'app_build.gradle') -Force
Copy-Item 'd:\Android\Projects\MyWeiXinclient\build.gradle' -Destination (Join-Path $client 'root_build.gradle') -Force
Copy-Item 'd:\Android\Projects\MyWeiXinclient\settings.gradle' -Destination $client -Force
Write-Host "Build files copied"

# 5. Copy server files
$serverSrc = 'C:\Users\lenovo\AppData\Local\Temp\MyWeiXinServer_src'
Get-ChildItem -Path $serverSrc | ForEach-Object {
    if ($_.PSIsContainer) {
        Copy-Item -Path $_.FullName -Destination (Join-Path $server $_.Name) -Recurse -Force
    } else {
        Copy-Item $_.FullName -Destination $server -Force
    }
}
Write-Host "Server files copied"

# 6. Print directory structure
Write-Host "`n===== push_temp directory structure ====="
Get-ChildItem -Path $base -Recurse | ForEach-Object {
    $indent = '  ' * ($_.FullName.Replace($base, '').Split('\').Count - 1)
    if ($_.PSIsContainer) {
        Write-Host "$indent$($_.Name)/"
    } else {
        Write-Host "$indent$($_.Name)"
    }
}
Write-Host "`nDone!"