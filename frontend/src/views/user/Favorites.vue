<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">我的收藏</h1>
      <p class="text-gray-600 mt-1">查看您收藏的题目，随时复习</p>
    </div>

    <!-- 收藏统计 -->
    <div class="card">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-gray-600">共收藏</p>
          <p class="text-2xl font-bold text-primary-600">{{ totalCount }}</p>
        </div>
        <div class="h-16 w-16 bg-yellow-100 rounded-full flex items-center justify-center">
          <svg class="h-8 w-8 text-yellow-600" fill="currentColor" viewBox="0 0 24 24">
            <path d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div class="card">
      <div v-if="loading" class="text-center py-12">
        <p class="text-gray-600">加载中...</p>
      </div>

      <div v-else-if="questions.length === 0" class="text-center py-12">
        <svg class="h-16 w-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
        </svg>
        <p class="text-gray-600 mb-2">还没有收藏任何题目</p>
        <p class="text-sm text-gray-500">在练习时可以收藏题目，方便以后复习</p>
      </div>

      <div v-else>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  题目内容
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  类型
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  难度
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  正确答案
                </th>
                <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  操作
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="(question, index) in questions" :key="question.id" class="hover:bg-gray-50">
                <td class="px-6 py-4">
                  <div class="text-sm text-gray-900 max-w-md">{{ question.question }}</div>
                  <div v-if="question.explanation" class="text-xs text-gray-500 mt-1">
                    解析: {{ question.explanation }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="px-2 py-1 text-xs font-medium rounded-full" :class="getTypeClass(question.type)">
                    {{ getTypeName(question.type) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span class="px-2 py-1 text-xs font-medium rounded-full" :class="getDifficultyClass(question.difficulty)">
                    {{ getDifficultyName(question.difficulty) }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-gray-900">
                    {{ String.fromCharCode(65 + question.correctAnswer) }}. {{ question.options[question.correctAnswer] }}
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                  <button
                    @click="viewQuestion(question)"
                    class="text-primary-600 hover:text-primary-900"
                  >
                    查看
                  </button>
                  <button
                    @click="removeFavorite(question.id, index)"
                    class="text-red-600 hover:text-red-900"
                  >
                    取消收藏
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 分页 -->
        <div class="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
          <div class="text-sm text-gray-700">
            显示 {{ (currentPage - 1) * pageSize + 1 }} 到 {{ Math.min(currentPage * pageSize, totalCount) }} 条，共 {{ totalCount }} 条
          </div>
          <div class="flex space-x-2">
            <button
              @click="changePage(currentPage - 1)"
              :disabled="currentPage === 1"
              class="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              上一页
            </button>
            <span class="px-4 py-2 text-sm text-gray-700">
              第 {{ currentPage }} / {{ totalPages }} 页
            </span>
            <button
              @click="changePage(currentPage + 1)"
              :disabled="currentPage >= totalPages"
              class="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 查看题目详情模态框 -->
    <div v-if="viewingQuestion" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-lg max-w-3xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-900">题目详情</h2>
            <button @click="viewingQuestion = null" class="text-gray-400 hover:text-gray-600">
              <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div v-if="viewingQuestion" class="space-y-6">
            <!-- 题目类型和难度 -->
            <div class="flex items-center space-x-3">
              <span class="px-3 py-1 bg-blue-100 text-blue-700 text-sm font-medium rounded-full">
                {{ getTypeName(viewingQuestion.type) }}
              </span>
              <span class="px-3 py-1 bg-gray-100 text-gray-700 text-sm font-medium rounded-full" :class="getDifficultyClass(viewingQuestion.difficulty)">
                {{ getDifficultyName(viewingQuestion.difficulty) }}
              </span>
            </div>

            <!-- 题目内容 -->
            <div>
              <h3 class="text-lg font-medium text-gray-900 mb-4">
                {{ viewingQuestion.question }}
              </h3>
            </div>

            <!-- 选项 -->
            <div class="space-y-3">
              <div
                v-for="(option, optIndex) in viewingQuestion.options"
                :key="optIndex"
                class="p-4 border-2 rounded-lg"
                :class="optIndex === viewingQuestion.correctAnswer ? 'border-green-500 bg-green-50' : 'border-gray-200'"
              >
                <div class="flex items-center">
                  <div
                    class="h-6 w-6 rounded-full border-2 flex items-center justify-center mr-3"
                    :class="optIndex === viewingQuestion.correctAnswer ? 'border-green-500 bg-green-500' : 'border-gray-300'"
                  >
                    <div
                      v-if="optIndex === viewingQuestion.correctAnswer"
                      class="h-3 w-3 rounded-full bg-white"
                    ></div>
                  </div>
                  <span class="font-medium mr-2">{{ String.fromCharCode(65 + optIndex) }}.</span>
                  <span>{{ option }}</span>
                  <span v-if="optIndex === viewingQuestion.correctAnswer" class="ml-2 text-green-600 font-medium">✓ 正确答案</span>
                </div>
              </div>
            </div>

            <!-- 答案解析 -->
            <div v-if="viewingQuestion.explanation" class="p-4 bg-blue-50 rounded-lg">
              <p class="text-sm font-medium text-blue-900 mb-1">答案解析：</p>
              <p class="text-sm text-blue-800">{{ viewingQuestion.explanation }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getFavorites, removeFavorite as apiRemoveFavorite } from '@/api/favorites'

const route = useRoute()
const authStore = useAuthStore()

const questions = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)
const viewingQuestion = ref(null)

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

const loadData = async () => {
  await loadFavorites()
}

onMounted(async () => {
  await loadData()
})

watch(() => route.path, async (newPath) => {
  if (newPath === '/user/favorites') {
    await loadData()
  }
}, { immediate: false })

const loadFavorites = async () => {
  loading.value = true
  try {
    const data = await getFavorites({
      page: currentPage.value,
      page_size: pageSize.value
    })
    questions.value = data.results || []
    totalCount.value = data.count || 0
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    questions.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

const changePage = async (page) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  await loadFavorites()
}

const removeFavorite = async (questionId, index) => {
  if (!confirm('确定要取消收藏这道题目吗？')) {
    return
  }
  
  try {
    await apiRemoveFavorite(questionId)
    questions.value.splice(index, 1)
    totalCount.value--
    
    // 如果当前页没有题目了，且不是第一页，则跳转到上一页
    if (questions.value.length === 0 && currentPage.value > 1) {
      currentPage.value--
      await loadFavorites()
    } else if (questions.value.length === 0) {
      // 如果第一页也没有题目了，重新加载
      await loadFavorites()
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    alert('取消收藏失败，请稍后重试')
  }
}

const viewQuestion = (question) => {
  viewingQuestion.value = question
}

const getTypeName = (type) => {
  return type === 'single' ? '单选题' : '判断题'
}

const getTypeClass = (type) => {
  return type === 'single' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700'
}

const getDifficultyName = (difficulty) => {
  const map = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return map[difficulty] || difficulty
}

const getDifficultyClass = (difficulty) => {
  const map = {
    easy: 'bg-green-100 text-green-700',
    medium: 'bg-yellow-100 text-yellow-700',
    hard: 'bg-red-100 text-red-700'
  }
  return map[difficulty] || 'bg-gray-100 text-gray-700'
}
</script>

