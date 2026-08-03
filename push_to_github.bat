@echo off
cd /d "d:\Android\Projects\MyWeiXinclient\push_temp"

REM 如果已经是 git 仓库，先重置
if exist ".git" (
    rmdir /s /q .git
)

git init
git remote add origin https://github.com/LIL-ZM/MyWeichat.git
git add .
git commit -m "feat: init project with client and server code"

REM 尝试推送到 main 分支，如果失败则尝试 master
git branch -M main
git push -u origin main --force
if %errorlevel% neq 0 (
    echo 尝试推送到 master 分支...
    git branch -M master
    git push -u origin master --force
)

echo.
echo 推送完成！
pause