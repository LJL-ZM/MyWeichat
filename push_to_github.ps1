Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
Set-Location "d:\Android\Projects\MyWeiXinclient\push_temp"

if (Test-Path ".git") {
    Remove-Item -Recurse -Force ".git"
}

git init
git remote add origin https://github.com/LIL-ZM/MyWeichat.git
git add .
git commit -m "feat: init project with client and server code"
git branch -M main
git push -u origin main --force

if ($LASTEXITCODE -ne 0) {
    Write-Host "尝试推送到 master 分支..."
    git branch -M master
    git push -u origin master --force
}

Write-Host "推送完成！"