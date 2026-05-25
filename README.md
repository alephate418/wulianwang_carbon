# 校园碳排放监测系统

## 项目简介

本项目是一个基于 Spring Boot + React 的校园碳排放监测系统，用于实时监测和管理校园内各区域的碳排放数据，支持数据统计、可视化展示和预警功能。

## 技术栈

### 后端技术栈
| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| Java | 17 | 编程语言 |
| Spring Boot | 4.0.0 | 后端框架 |
| MyBatis | 4.0.0 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Maven | 3.6+ | 构建工具 |

### 前端技术栈
| 技术 | 版本 | 说明 |
| :--- | :--- | :--- |
| React | 19.2.0 | 前端框架 |
| TypeScript | ~5.9.3 | 类型安全 |
| Vite | 7.2.4 | 构建工具 |
| Tailwind CSS | 4.1.17 | CSS框架 |
| React Router | 7.10.1 | 路由管理 |
| Recharts | 3.5.1 | 图表库 |
| Lucide React | 0.556.0 | 图标库 |

## 快速开始

### 环境要求

- Java 17+
- Node.js 20+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库配置

创建MySQL数据库：

```sql
CREATE DATABASE carbon_monitor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行数据库初始化脚本：

```bash
mysql -u root -p carbon_monitor < carbon-monitor-backend-test/src/main/resources/database/ceateTable.sql
```

### 2. 运行后端服务

```bash
cd carbon-monitor-backend-test
mvn spring-boot:run
```

服务启动后访问地址：`http://localhost:8080/api`

### 3. 运行前端服务

```bash
cd wulianwang_carbon/wulianwang_carbon
npm install
npm run dev
```

前端启动后访问地址：`http://localhost:5173`

## 项目结构

```
├── carbon-monitor-backend-test/    # 后端项目
│   ├── src/main/java/ynu/edu/
│   │   ├── controller/             # 控制层
│   │   ├── service/                # 服务层
│   │   ├── mapper/                 # 数据访问层
│   │   ├── entity/                 # 实体类
│   │   ├── config/                 # 配置类
│   │   ├── task/                   # 定时任务
│   │   └── util/                   # 工具类
│   ├── src/main/resources/
│   │   ├── database/               # 数据库脚本
│   │   └── ynu/edu/mapper/         # MyBatis映射文件
│   └── pom.xml
├── wulianwang_carbon/wulianwang_carbon/  # 前端项目
│   ├── src/
│   │   ├── components/             # 组件
│   │   ├── services/               # API服务
│   │   ├── App.tsx                 # 主应用组件
│   │   └── main.tsx                # 入口文件
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts
│   └── tailwind.config.js
└── README.md
```

## API接口

### 区域管理
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/api/areas` | 获取所有区域列表 |
| GET | `/api/areas/{id}` | 获取单个区域详情 |
| POST | `/api/areas` | 新增区域 |
| PUT | `/api/areas/{id}` | 更新区域 |
| DELETE | `/api/areas/{id}` | 删除区域 |

### 碳排放数据
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/api/carbon` | 获取碳排放数据列表 |
| GET | `/api/carbon/statistics` | 获取统计数据 |

### 预警管理
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/api/warnings` | 获取预警列表 |
| PUT | `/api/warnings/{id}/handle` | 处理预警 |

### 系数管理
| 方法 | 路径 | 说明 |
| :--- | :--- | :--- |
| GET | `/api/coefficients` | 获取系数列表 |
| POST | `/api/coefficients` | 新增系数 |

## 功能特性

- 校园区域管理（增删改查）
- 碳排放数据统计与展示
- 碳排放预警系统
- 碳排放系数配置
- 数据可视化图表
- 数据导入功能

## 许可证

MIT License
