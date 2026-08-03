# MyWeichat - 简易聊天软件

一个基于 Android + Java 后端的仿微信聊天应用。

## 项目结构

```
MyWeichat/
├── client/          # Android 客户端源码
│   ├── java/        # Java 源代码
│   ├── res/         # 资源文件（布局、图片、字符串等）
│   └── AndroidManifest.xml
├── server/          # Java 后端源码
│   ├── http/        # HTTP 接口处理
│   ├── myweixin/    # 数据访问层和业务逻辑
│   └── socket/      # Socket 长连接推送服务
└── README.md
```

## 技术栈

### 客户端
- **语言**: Java
- **UI**: Android SDK, ViewBinding, RecyclerView
- **网络**: OkHttp (HTTP), Socket (TCP 长连接)
- **存储**: SQLite (本地数据库), SharedPreferences
- **JSON**: Gson

### 服务端
- **语言**: Java
- **数据库**: MySQL (JDBC)
- **通信**: 自研 HTTP 服务器 (ServerSocket), Socket 长连接
- **JSON**: Gson

## 功能特性

- 用户注册/登录
- 单聊/群聊
- 好友添加/申请
- 实时消息推送 (Socket)
- 离线消息补偿
- 消息持久化存储
- 未读消息计数
- 断线自动重连

## 快速开始

### 服务端部署

1. 将 `server/` 目录下的 Java 文件编译并运行
2. 确保 MySQL 数据库运行，并修改 `SqlManager.java` 中的数据库配置：
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/your_database?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
   private static final String USER = "root";
   private static final String PWD = "your_password";
   ```
3. 初始化数据库表结构（参考 `db_schema.sql`）
4. 运行 `HttpServer.java` (端口 8082) 和 `SocketServer.java` (端口 8085)

### 客户端配置

1. 打开 `client/java/HttpUtil.java`，修改服务器地址：
   ```java
   private static final String BASE_URL = "http://YOUR_SERVER_IP:8082";
   ```
2. 打开 `client/java/MainActivity.java`，修改 Socket 连接地址：
   ```java
   SocketClientManager.getInstance().connect(this, SpUtil.getUid(), "YOUR_SERVER_IP", 8085);
   ```
3. 使用 Android Studio 打开 `client/` 目录，编译运行

## 核心通信机制

- **HTTP**: 负责业务请求（登录、发消息、拉会话等）
- **Socket**: 负责实时消息推送（新消息、好友申请）
- **离线补偿**: 用户登录时自动拉取未读消息

## 注意事项

- 本项目仅用于学习交流，生产环境需增加 HTTPS、密码加密、SQL 注入防护等安全措施
- 替换敏感信息后请勿公开原始密码
