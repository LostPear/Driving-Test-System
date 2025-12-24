import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue')
  },
  {
    path: '/user',
    name: 'UserLayout',
    component: () => import('@/views/user/Layout.vue'),
    meta: { requiresAuth: true, role: 'user' },
    children: [
      {
        path: '',
        redirect: '/user/dashboard'
      },
      {
        path: 'dashboard',
        name: 'UserDashboard',
        component: () => import('@/views/user/Dashboard.vue')
      },
      {
        path: 'practice',
        name: 'Practice',
        component: () => import('@/views/user/Practice.vue')
      },
      {
        path: 'practice/sequential',
        name: 'SequentialPractice',
        component: () => import('@/views/user/SequentialPractice.vue')
      },
      {
        path: 'practice/random',
        name: 'RandomPractice',
        component: () => import('@/views/user/RandomPractice.vue')
      },
      {
        path: 'exam',
        name: 'Exam',
        component: () => import('@/views/user/Exam.vue')
      },
      {
        path: 'exam/result/:id',
        name: 'ExamResult',
        component: () => import('@/views/user/ExamResult.vue')
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/user/Favorites.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue')
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'questions',
        name: 'QuestionManagement',
        component: () => import('@/views/admin/QuestionManagement.vue')
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue')
      },
      {
        path: 'import',
        name: 'QuestionImport',
        component: () => import('@/views/admin/QuestionImport.vue')
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth) {
    if (!authStore.isAuthenticated) {
      next('/login')
    } else if (to.meta.role && to.meta.role !== authStore.userRole) {
      // 角色不匹配，重定向到对应角色的页面
      if (authStore.userRole === 'admin') {
        next('/admin')
      } else {
        next('/user')
      }
    } else {
      next()
    }
  } else {
    // 如果已登录，访问登录页时重定向
    if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
      if (authStore.userRole === 'admin') {
        next('/admin')
      } else {
        next('/user')
      }
    } else {
      next()
    }
  }
})

export default router
