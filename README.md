# 上会回收项目

## 项目说明
上会回收项目 - 卖方用户发起回收请求，回收方承接、取货、入库、财务统计。

## 项目结构

### recycle-backend (Spring Boot 后端)
- 端口：8090
- 技术栈：Spring Boot 3.0 + MyBatis-Plus + JWT + Redis + 阿里云短信
- 默认管理员：admin / admin123

### recycle-front-mobile (H5 移动端)
- 技术栈：uni-app + Vue3 + Pinia
- 用户无需登录，手机号即可发起回收请求

### recycle-front-platform (管理后台)
- 端口：3000
- 技术栈：Vue3 + Vite + Pinia + Element Plus
- 自动路由，JWT登录校验

## 数据库
- 导入桌面 `recycle_db.sql` 到 MySQL
- 数据库名：recycle_db

## 启动步骤
1. 导入SQL到MySQL数据库
2. 启动Redis
3. 配置 application.yml 中的阿里云短信参数
4. 启动后端：`mvn spring-boot:run`
5. 启动移动端：`npm run dev:h5`
6. 启动后台：`npm run dev`
