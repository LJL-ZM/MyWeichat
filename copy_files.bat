@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "BASE=d:\Android\Projects\MyWeiXinclient\push_temp"
set "CLIENT=%BASE%\client"
set "SERVER=%BASE%\server"

echo ========================================
echo   创建目录结构...
echo ========================================

if not exist "%CLIENT%\java" mkdir "%CLIENT%\java"
if not exist "%CLIENT%\res" mkdir "%CLIENT%\res"
if not exist "%SERVER%" mkdir "%SERVER%"

echo.
echo [1/5] 复制客户端 Java 文件...
set "JAVA_SRC=d:\Android\Projects\MyWeiXinclient\app\src\main\java\com\example\myweixin_client\src-client\java"
copy "%JAVA_SRC%\*.java" "%CLIENT%\java\" /Y >nul
for %%f in ("%JAVA_SRC%\*.java") do set /a count+=1
echo       已复制 %count% 个 Java 文件

echo.
echo [2/5] 复制 res 资源目录...
xcopy "d:\Android\Projects\MyWeiXinclient\app\src\main\res" "%CLIENT%\res\" /E /I /Y /Q >nul
echo       已复制 res 资源目录

echo.
echo [3/5] 复制 AndroidManifest.xml...
copy "d:\Android\Projects\MyWeiXinclient\app\src\main\AndroidManifest.xml" "%CLIENT%\" /Y >nul
echo       已复制

echo.
echo [4/5] 复制 build.gradle 和 settings.gradle...
copy "d:\Android\Projects\MyWeiXinclient\app\build.gradle" "%CLIENT%\app_build.gradle" /Y >nul
copy "d:\Android\Projects\MyWeiXinclient\build.gradle" "%CLIENT%\root_build.gradle" /Y >nul
copy "d:\Android\Projects\MyWeiXinclient\settings.gradle" "%CLIENT%\" /Y >nul
echo       已复制

echo.
echo [5/5] 复制服务端代码...
set "SERVER_SRC=C:\Users\lenovo\AppData\Local\Temp\MyWeiXinServer_src"
xcopy "%SERVER_SRC%\*" "%SERVER%\" /E /I /Y /Q >nul
echo       已复制服务端代码

echo.
echo ========================================
echo   push_temp 目录结构:
echo ========================================
call :tree "%BASE%" 0
echo.
echo 完成!
pause
exit /b

:tree
setlocal
set "cur=%~1"
set "depth=%~2"
for /f "delims=" %%d in ('dir /b /ad "%cur%" 2^>nul') do (
    set /a nextDepth=%depth%+1
    if !depth! LEQ 2 (
        for /l %%i in (0,1,!depth!) do set "indent=  !indent!"
        echo !indent!%%d\
        for /f "delims=" %%f in ('dir /b "%cur%\%%d" 2^>nul') do (
            set "fname=%%f"
            echo !indent!  !fname!
        )
        call :tree "%cur%\%%d" !nextDepth!
    )
)
endlocal