import os
import shutil

BASE = r'd:\Android\Projects\MyWeiXinclient\push_temp'
CLIENT = os.path.join(BASE, 'client')
SERVER = os.path.join(BASE, 'server')

for d in [os.path.join(CLIENT, 'java'), os.path.join(CLIENT, 'res'), SERVER]:
    os.makedirs(d, exist_ok=True)

print("目录结构已创建")

# 1. 复制客户端 Java 文件
java_src = r'd:\Android\Projects\MyWeiXinclient\app\src\main\java\com\example\myweixin_client\src-client\java'
java_dst = os.path.join(CLIENT, 'java')
for fname in os.listdir(java_src):
    if fname.endswith('.java'):
        shutil.copy2(os.path.join(java_src, fname), os.path.join(java_dst, fname))
print(f"已复制 Java 文件: {len([f for f in os.listdir(java_src) if f.endswith('.java')])} 个")

# 2. 复制 res 资源目录
res_src = r'd:\Android\Projects\MyWeiXinclient\app\src\main\res'
res_dst = os.path.join(CLIENT, 'res')
if os.path.exists(res_dst):
    shutil.rmtree(res_dst)
shutil.copytree(res_src, res_dst)
print("已复制 res 资源目录")

# 3. 复制 AndroidManifest.xml
shutil.copy2(
    r'd:\Android\Projects\MyWeiXinclient\app\src\main\AndroidManifest.xml',
    os.path.join(CLIENT, 'AndroidManifest.xml')
)
print("已复制 AndroidManifest.xml")

# 4. 复制 build.gradle 和 settings.gradle
shutil.copy2(
    r'd:\Android\Projects\MyWeiXinclient\app\build.gradle',
    os.path.join(CLIENT, 'app_build.gradle')
)
shutil.copy2(
    r'd:\Android\Projects\MyWeiXinclient\build.gradle',
    os.path.join(CLIENT, 'root_build.gradle')
)
shutil.copy2(
    r'd:\Android\Projects\MyWeiXinclient\settings.gradle',
    os.path.join(CLIENT, 'settings.gradle')
)
print("已复制 build.gradle 和 settings.gradle")

# 5. 复制服务端代码
server_src = r'C:\Users\lenovo\AppData\Local\Temp\MyWeiXinServer_src'
for item in os.listdir(server_src):
    s = os.path.join(server_src, item)
    d = os.path.join(SERVER, item)
    if os.path.isdir(s):
        if os.path.exists(d):
            shutil.rmtree(d)
        shutil.copytree(s, d)
    else:
        shutil.copy2(s, d)
print("已复制服务端代码")

# 6. 打印目录结构
print("\n===== push_temp 目录结构 =====")
for root, dirs, files in os.walk(BASE):
    level = root.replace(BASE, '').count(os.sep)
    indent = ' ' * 2 * level
    print(f'{indent}{os.path.basename(root)}/')
    subindent = ' ' * 2 * (level + 1)
    for file in files:
        print(f'{subindent}{file}')

print("\n完成!")