<template>
  <div class="space-y-6">
    <div class="card">
      <h1 class="text-2xl font-bold text-gray-900 mb-2">练习模式</h1>
      <p class="text-gray-600">选择练习方式，提升您的驾照理论知识</p>
    </div>

    <!-- 分类练习 -->
    <div class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-4">分类练习</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div
          class="p-4 border-2 border-blue-200 rounded-lg hover:border-blue-500 hover:shadow-md transition-all cursor-pointer"
          @click="startTypePractice('single')"
        >
          <div class="flex items-center justify-center h-16 bg-blue-100 rounded-lg mb-3">
            <svg class="h-8 w-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-1">单选题</h3>
          <p class="text-sm text-gray-600">专门练习单选题</p>
        </div>
        <div
          class="p-4 border-2 border-purple-200 rounded-lg hover:border-purple-500 hover:shadow-md transition-all cursor-pointer"
          @click="startTypePractice('multiple')"
        >
          <div class="flex items-center justify-center h-16 bg-purple-100 rounded-lg mb-3">
            <svg class="h-8 w-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-1">多选题</h3>
          <p class="text-sm text-gray-600">专门练习多选题</p>
        </div>
        <div
          class="p-4 border-2 border-green-200 rounded-lg hover:border-green-500 hover:shadow-md transition-all cursor-pointer"
          @click="startTypePractice('judge')"
        >
          <div class="flex items-center justify-center h-16 bg-green-100 rounded-lg mb-3">
            <svg class="h-8 w-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold text-gray-900 mb-1">判断题</h3>
          <p class="text-sm text-gray-600">专门练习判断题</p>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- 顺序练习 -->
      <div class="card hover:shadow-lg transition-shadow cursor-pointer" @click="$router.push('/user/practice/sequential')">
        <div class="flex items-center justify-center h-32 bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg mb-4">
          <svg class="h-16 w-16 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">顺序练习</h2>
        <p class="text-gray-600 mb-4">按照题库顺序依次练习，系统记录您的练习进度</p>
        <div class="flex items-center text-primary-600 font-medium">
          开始练习
          <svg class="ml-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
          </svg>
        </div>
      </div>

      <!-- 随机练习 -->
      <div class="card hover:shadow-lg transition-shadow cursor-pointer" @click="$router.push('/user/practice/random')">
        <div class="flex items-center justify-center h-32 bg-gradient-to-br from-green-500 to-green-600 rounded-lg mb-4">
          <svg class="h-16 w-16 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
          </svg>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">随机练习</h2>
        <p class="text-gray-600 mb-4">随机抽取题目进行练习，加强知识点巩固</p>
        <div class="flex items-center text-primary-600 font-medium">
          开始练习
          <svg class="ml-2 h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7l5 5m0 0l-5 5m5-5H6" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 练习统计 -->
    <div class="card">
      <h2 class="text-xl font-bold text-gray-900 mb-4">练习统计</h2>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="text-center p-4 bg-blue-50 rounded-lg">
          <p class="text-2xl font-bold text-blue-600">{{ practiceStats.total }}</p>
          <p class="text-sm text-gray-600 mt-1">已练习题目</p>
        </div>
        <div class="text-center p-4 bg-green-50 rounded-lg">
          <p class="text-2xl font-bold text-green-600">{{ practiceStats.correct }}</p>
          <p class="text-sm text-gray-600 mt-1">答对题目</p>
        </div>
        <div class="text-center p-4 bg-red-50 rounded-lg">
          <p class="text-2xl font-bold text-red-600">{{ practiceStats.wrong }}</p>
          <p class="text-sm text-gray-600 mt-1">答错题目</p>
        </div>
        <div class="text-center p-4 bg-purple-50 rounded-lg">
          <p class="text-2xl font-bold text-purple-600">{{ practiceStats.accuracy }}%</p>
          <p class="text-sm text-gray-600 mt-1">正确率</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUserStats } from '@/api/users'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const practiceStats = ref({
  total: 0,
  correct: 0,
  wrong: 0,
  accuracy: 0
})

// 加载数据的函数
const loadData = async () => {
  await loadPracticeStats()
}

onMounted(async () => {
  await loadData()
})

// 监听路由变化，每次进入页面时刷新数据
watch(() => route.path, async (newPath) => {
  if (newPath === '/user/practice') {
    await loadData()
  }
}, { immediate: false })

const loadPracticeStats = async () => {
  try {
    const data = await getUserStats(authStore.user?.id)
    practiceStats.value = {
      total: data.practiceTotal || 0,
      correct: data.practiceCorrect || 0,
      wrong: data.practiceWrong || 0,
      accuracy: data.practiceAccuracy || 0
    }
  } catch (error) {
    console.error('加载练习统计数据失败:', error)
    practiceStats.value = {
      total: 0,
      correct: 0,
      wrong: 0,
      accuracy: 0
    }
  }
}

const startTypePractice = (type) => {
  router.push({
    path: '/user/practice/random',
    query: { type }
  })
}
</script>
