# 驾照理论模拟考试系统 - 前端

一个基于 Vue 3 + Vite + TailwindCSS 的驾照理论模拟考试系统前端应用。

## 功能特性

### 用户功能
- ✅ **用户认证**
  - 用户注册（邮箱、用户名、密码）
  - 用户登录（用户名密码验证）
  - 登录失败提醒

- ✅ **练习模式**
  - 顺序练习：按题库顺序依次练习
  - 随机练习：随机抽取题目练习
  - 实时答题反馈和解析

- ✅ **模拟考试**
  - 从题库中随机抽取题目组卷
  - 计时功能（45分钟）
  - 答题卡功能
  - 考试结果展示和错题解析
  - 考试历史记录

- ✅ **个人中心**
  - 个人资料修改
  - 密码修改
  - 学习统计数据

### 管理员功能
- ✅ **题库管理**
  - 题目列表查看
  - 添加新题目
  - 编辑题目
  - 删除题目
  - 题目搜索和筛选

- ✅ **用户管理**
  - 用户列表查看
  - 用户信息编辑
  - 用户删除
  - 用户学习数据查看

- ✅ **题库导入**
  - 支持 Excel、CSV、JSON 格式
  - 拖拽上传
  - 批量导入
  - 导入历史记录

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **样式**: TailwindCSS
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP 客户端**: Axios

## 项目结构

```
src/
├── api/              # API 接口
│   ├── client.js     # Axios 配置
│   ├── auth.js       # 认证相关 API
│   ├── questions.js  # 题目相关 API
│   ├── exam.js       # 考试相关 API
│   └── users.js      # 用户相关 API
├── stores/           # Pinia 状态管理
│   └── auth.js       # 认证状态
├── router/           # 路由配置
│   └── index.js      # 路由定义
├── views/            # 页面组件
│   ├── auth/         # 认证页面
│   │   ├── Login.vue
│   │   └── Register.vue
│   ├── user/         # 用户页面
│   │   ├── Layout.vue
│   │   ├── Dashboard.vue
│   │   ├── Practice.vue
│   │   ├── SequentialPractice.vue
│   │   ├── RandomPractice.vue
│   │   ├── Exam.vue
│   │   ├── ExamResult.vue
│   │   └── Profile.vue
│   └── admin/        # 管理员页面
│       ├── Layout.vue
│       ├── Dashboard.vue
│       ├── QuestionManagement.vue
│       ├── UserManagement.vue
│       └── QuestionImport.vue
├── App.vue           # 根组件
├── main.js           # 入口文件
└── style.css         # 全局样式

```

## 安装和运行

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

应用将在 `http://localhost:3001` 运行。

### 生产构建

```bash
npm run build
```

构建产物将输出到 `dist/` 目录。

### 预览生产构建

```bash
npm run preview
```

## 后端 API 配置

前端默认配置了代理，所有 `/api` 开头的请求会被代理到 `http://localhost:8000`。

你可以在 `vite.config.js` 中修改后端地址：

```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://your-backend-url:8000',  // 修改为你的后端地址
        changeOrigin: true
      }
    }
  }
})
```

## 后端 API 接口规范

### 认证接口

- `POST /api/auth/register/` - 用户注册
  ```json
  {
    "username": "string",
    "email": "string",
    "password": "string"
  }
  ```

- `POST /api/auth/login/` - 用户登录
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
  返回：
  ```json
  {
    "token": "string",
    "user": {
      "id": "number",
      "username": "string",
      "email": "string",
      "role": "user|admin"
    }
  }
  ```

- `POST /api/auth/logout/` - 用户登出

- `PUT /api/auth/profile/` - 更新个人资料

- `POST /api/auth/change-password/` - 修改密码

### 题目接口

- `GET /api/questions/` - 获取题目列表
- `GET /api/questions/:id/` - 获取单个题目
- `POST /api/questions/` - 创建题目
- `PUT /api/questions/:id/` - 更新题目
- `DELETE /api/questions/:id/` - 删除题目
- `GET /api/questions/random/` - 获取随机题目
- `GET /api/questions/sequential/` - 获取顺序题目
- `POST /api/questions/import/` - 批量导入题目

### 考试接口

- `POST /api/exams/` - 创建考试
- `GET /api/exams/:id/` - 获取考试详情
- `POST /api/exams/:id/submit/` - 提交考试
- `GET /api/exams/:id/result/` - 获取考试结果
- `GET /api/exams/history/` - 获取考试历史

### 用户管理接口

- `GET /api/users/` - 获取用户列表
- `GET /api/users/:id/` - 获取用户详情
- `PUT /api/users/:id/` - 更新用户
- `DELETE /api/users/:id/` - 删除用户
- `GET /api/users/:id/stats/` - 获取用户统计

## Django 后端开发建议

### 1. 安装必要的包

```bash
pip install django djangorestframework django-cors-headers djangorestframework-simplejwt
```

### 2. Django 设置

在 `settings.py` 中配置：

```python
INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'rest_framework',
    'corsheaders',
    'your_app',
]

MIDDLEWARE = [
    'corsheaders.middleware.CorsMiddleware',
    'django.middleware.security.SecurityMiddleware',
    # ... 其他中间件
]

# CORS 配置
CORS_ALLOWED_ORIGINS = [
    "http://localhost:3000",
]

# REST Framework 配置
REST_FRAMEWORK = {
    'DEFAULT_AUTHENTICATION_CLASSES': [
        'rest_framework_simplejwt.authentication.JWTAuthentication',
    ],
    'DEFAULT_PERMISSION_CLASSES': [
        'rest_framework.permissions.IsAuthenticated',
    ],
}
```

### 3. 数据模型建议

```python
from django.db import models
from django.contrib.auth.models import User

class Question(models.Model):
    TYPE_CHOICES = [
        ('single', '单选题'),
        ('judge', '判断题'),
    ]
    DIFFICULTY_CHOICES = [
        ('easy', '简单'),
        ('medium', '中等'),
        ('hard', '困难'),
    ]
    
    type = models.CharField(max_length=10, choices=TYPE_CHOICES)
    difficulty = models.CharField(max_length=10, choices=DIFFICULTY_CHOICES)
    question = models.TextField()
    options = models.JSONField()  # 存储选项数组
    correct_answer = models.IntegerField()
    explanation = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    
class Exam(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    questions = models.ManyToManyField(Question)
    answers = models.JSONField()  # 存储用户答案
    score = models.IntegerField(null=True)
    passed = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    submitted_at = models.DateTimeField(null=True)
```

## 默认账户

开发模式下，你可以使用以下模拟数据：

- 普通用户：`username: user`, `password: password`
- 管理员：`username: admin`, `password: admin`

注意：实际部署时需要连接真实的后端 API。

## 特性说明

### 路由守卫
- 自动检查用户登录状态
- 根据用户角色（user/admin）自动重定向到相应页面
- 未登录用户访问受保护页面会被重定向到登录页

### 响应式设计
- 支持桌面端和移动端
- 使用 TailwindCSS 实现响应式布局

### 用户体验
- 实时表单验证
- 友好的错误提示
- 加载状态显示
- 平滑的页面过渡动画

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## License

MIT
