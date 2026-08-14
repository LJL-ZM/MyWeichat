#!/bin/bash
# 清理旧的推送目录
rm -rf push_temp
mkdir -p push_temp/client
mkdir -p push_temp/server

# 复制客户端源码和资源
# Java 源码
mkdir -p push_temp/client/app/src/main/java/com/example/myweixin_client
cp -r app/src/main/java/com/example/myweixin_client/* push_temp/client/app/src/main/java/com/example/myweixin_client/

# XML 布局和 Drawable
mkdir -p push_temp/client/app/src/main/res
cp -r app/src/main/res/* push_temp/client/app/src/main/res/

# AndroidManifest.xml
cp app/src/main/AndroidManifest.xml push_temp/client/app/src/main/

# Gradle 配置
cp app/build.gradle push_temp/client/app/
cp build.gradle push_temp/client/
cp settings.gradle push_temp/client/

# 复制服务端源码
cp -r C:/Users/lenovo/AppData/Local/Temp/MyWeiXinServer_src/* push_temp/server/

echo "目录结构构建完成！"
ls -la push_temp/client/
echo "---"
ls -la push_temp/server/
