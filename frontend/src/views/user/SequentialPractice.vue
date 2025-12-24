<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <!-- 顶部进度条 -->
    <div class="card">
      <div class="flex items-center justify-between mb-2">
        <h2 class="text-lg font-semibold text-gray-900">顺序练习</h2>
        <span class="text-sm text-gray-600">题目 {{ currentIndex + 1 }} / {{ questions.length }}</span>
      </div>
      <div class="w-full bg-gray-200 rounded-full h-2">
        <div
          class="bg-primary-600 h-2 rounded-full transition-all duration-300"
          :style="{ width: `${progress}%` }"
        ></div>
      </div>
    </div>

    <!-- 题目卡片 -->
    <div v-if="currentQuestion" class="card">
      <!-- 题目类型标签 -->
      <div class="flex items-center justify-between mb-4">
        <span class="px-3 py-1 bg-blue-100 text-blue-700 text-sm font-medium rounded-full">
          {{ getTypeName(currentQuestion.type) }}
        </span>
        <button
          @click="toggleBookmark"
          class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <svg
            class="h-6 w-6"
            :class="isBookmarked ? 'text-yellow-500 fill-current' : 'text-gray-400'"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
          </svg>
        </button>
      </div>

      <!-- 题目内容 -->
      <div class="mb-6">
        <h3 class="text-xl font-medium text-gray-900 mb-3">
          {{ currentQuestion.question }}
        </h3>
        <div v-if="currentQuestion.image" class="mt-4">
          <img :src="getImageUrl(currentQuestion.image)" alt="题目图片" class="max-w-full h-auto max-h-96 rounded-lg border border-gray-300 shadow-sm" />
        </div>
      </div>

      <!-- 选项 -->
      <div class="space-y-3">
        <div
          v-for="(option, index) in currentQuestion.options"
          :key="index"
          @click="selectOption(index)"
          class="p-4 border-2 rounded-lg cursor-pointer transition-all"
          :class="getOptionClass(index)"
        >
          <div class="flex items-center">
            <!-- 多选题使用复选框，单选题和判断题使用圆形 -->
            <div
              v-if="currentQuestion.type === 'multiple'"
              class="h-5 w-5 border-2 rounded flex items-center justify-center mr-3"
              :class="getCheckboxClass(index)"
            >
              <svg v-if="isSelected(index)" class="h-4 w-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
              </svg>
            </div>
            <div
              v-else
              class="h-6 w-6 rounded-full border-2 flex items-center justify-center mr-3"
              :class="getOptionCircleClass(index)"
            >
              <div
                v-if="selectedOption === index"
                class="h-3 w-3 rounded-full"
                :class="getOptionDotClass(index)"
              ></div>
            </div>
            <span class="font-medium mr-2">{{ String.fromCharCode(65 + index) }}.</span>
            <span>{{ option }}</span>
          </div>
        </div>
      </div>

      <!-- 答案解析 -->
      <transition name="fade">
        <div v-if="showAnswer" class="mt-6 p-4 rounded-lg" :class="isCorrect ? 'bg-green-50' : 'bg-red-50'">
          <div class="flex items-center mb-2">
            <svg
              v-if="isCorrect"
              class="h-6 w-6 text-green-600 mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
            <svg
              v-else
              class="h-6 w-6 text-red-600 mr-2"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
            <span class="font-semibold" :class="isCorrect ? 'text-green-900' : 'text-red-900'">
              {{ isCorrect ? '回答正确！' : '回答错误' }}
            </span>
          </div>
          <p class="text-sm text-gray-700 mb-1">
            <span class="font-medium">正确答案：</span>
            <span v-if="currentQuestion.type === 'multiple'">
              {{ getCorrectAnswerText() }}
            </span>
            <span v-else>
            {{ String.fromCharCode(65 + currentQuestion.correctAnswer) }}
            </span>
          </p>
          <p class="text-sm text-gray-700">
            <span class="font-medium">解析：</span>
            {{ currentQuestion.explanation }}
          </p>
        </div>
      </transition>

      <!-- 操作按钮 -->
      <div class="flex items-center justify-between mt-6 pt-6 border-t border-gray-200">
        <button
          @click="previousQuestion"
          :disabled="currentIndex === 0"
          class="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          上一题
        </button>
        
        <button
          v-if="!showAnswer"
          @click="submitAnswer"
          :disabled="currentQuestion?.type === 'multiple' ? selectedOptions.length === 0 : selectedOption === null"
          class="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
        >
          提交答案
        </button>
        
        <button
          v-else
          @click="nextQuestion"
          class="btn-primary"
        >
          {{ currentIndex < questions.length - 1 ? '下一题' : '完成练习' }}
        </button>
      </div>
    </div>

    <!-- 练习统计 -->
    <div class="card">
      <div class="grid grid-cols-3 gap-4 text-center">
        <div>
          <p class="text-2xl font-bold text-primary-600">{{ stats.answered }}</p>
          <p class="text-sm text-gray-600">已答题目</p>
        </div>
        <div>
          <p class="text-2xl font-bold text-green-600">{{ stats.correct }}</p>
          <p class="text-sm text-gray-600">答对</p>
        </div>
        <div>
          <p class="text-2xl font-bold text-red-600">{{ stats.wrong }}</p>
          <p class="text-sm text-gray-600">答错</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getSequentialQuestions } from '@/api/questions'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorites'
import { submitExam } from '@/api/exam'
import { getImageUrl } from '@/api/images'

const router = useRouter()
const authStore = useAuthStore()

const questions = ref([])
const currentIndex = ref(0)
const selectedOption = ref(null)
const selectedOptions = ref([]) // 用于多选题
const showAnswer = ref(false)
const favoriteIds = ref(new Set())
const practiceAnswers = ref({}) // 存储所有题目的答案
const isRecordSaved = ref(false) // 标记是否已保存记录
const stats = ref({
  answered: 0,
  correct: 0,
  wrong: 0
})

const currentQuestion = computed(() => questions.value[currentIndex.value])
const progress = computed(() => ((currentIndex.value + 1) / questions.value.length) * 100)
const isCorrect = computed(() => {
  if (!currentQuestion.value) return false
  if (currentQuestion.value.type === 'multiple') {
    // 多选题：比较选中的选项数组和正确答案数组
    const correctAnswers = currentQuestion.value.correctAnswers || [currentQuestion.value.correctAnswer]
    const selected = [...selectedOptions.value].sort()
    const correct = [...correctAnswers].sort()
    return selected.length === correct.length && selected.every((val, idx) => val === correct[idx])
  } else {
    // 单选题和判断题
    return selectedOption.value === currentQuestion.value.correctAnswer
  }
})
const isBookmarked = computed(() => {
  if (!currentQuestion.value) return false
  return favoriteIds.value.has(currentQuestion.value.id)
})

onMounted(async () => {
  await loadQuestions()
  await loadFavoriteIds()
})

const loadQuestions = async () => {
  try {
    const data = await getSequentialQuestions({ limit: 50 })
    questions.value = data.results || []
    practiceAnswers.value = {} // 重置答案记录
    stats.value = { answered: 0, correct: 0, wrong: 0 } // 重置统计
    isRecordSaved.value = false // 重置保存标记
  } catch (error) {
    console.error('加载题目失败:', error)
    questions.value = []
  }
}

const selectOption = (index) => {
  if (!showAnswer.value && currentQuestion.value) {
    if (currentQuestion.value.type === 'multiple') {
      // 多选题：切换选中状态
      const idx = selectedOptions.value.indexOf(index)
      if (idx > -1) {
        selectedOptions.value.splice(idx, 1)
      } else {
        selectedOptions.value.push(index)
      }
    } else {
      // 单选题和判断题：单选
    selectedOption.value = index
  }
  }
}

const isSelected = (index) => {
  if (currentQuestion.value?.type === 'multiple') {
    return selectedOptions.value.includes(index)
  }
  return selectedOption.value === index
}

const submitAnswer = () => {
  if (currentQuestion.value?.type === 'multiple') {
    if (selectedOptions.value.length === 0) return
    // 多选题：保存第一个选中的答案（简化处理）
    practiceAnswers.value[currentQuestion.value.id] = selectedOptions.value[0]
  } else {
  if (selectedOption.value === null) return
    practiceAnswers.value[currentQuestion.value.id] = selectedOption.value
  }
  
  showAnswer.value = true
  stats.value.answered++
  
  if (isCorrect.value) {
    stats.value.correct++
  } else {
    stats.value.wrong++
  }
}

const nextQuestion = async () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
    selectedOption.value = null
    selectedOptions.value = []
    showAnswer.value = false
  } else {
    // 完成练习，保存记录到数据库
    await savePracticeRecord()
    router.push('/user/dashboard')
  }
}

const savePracticeRecord = async () => {
  // 如果已经保存过，不再重复保存
  if (isRecordSaved.value) {
    return
  }
  
  // 如果没有答案，不保存
  if (Object.keys(practiceAnswers.value).length === 0) {
    return
  }
  
  try {
    // 只收集已提交答案的题目ID（不包括未做的题目）
    const questionIds = Object.keys(practiceAnswers.value).map(id => parseInt(id))
    
    // 调用API保存练习记录
    await submitExam(questionIds, practiceAnswers.value, 'practice')
    isRecordSaved.value = true
  } catch (error) {
    console.error('保存练习记录失败:', error)
    // 不阻止用户继续，静默失败
  }
}

// 在组件卸载前保存记录
onBeforeUnmount(async () => {
  await savePracticeRecord()
})

// 在路由离开前保存记录
onBeforeRouteLeave(async (to, from, next) => {
  await savePracticeRecord()
  next()
})

const previousQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    selectedOption.value = null
    selectedOptions.value = []
    showAnswer.value = false
  }
}

const getCorrectAnswerText = () => {
  if (!currentQuestion.value) return ''
  const correctAnswers = currentQuestion.value.correctAnswers || [currentQuestion.value.correctAnswer]
  return correctAnswers.map(idx => String.fromCharCode(65 + idx)).join('、')
}

const loadFavoriteIds = async () => {
  try {
    const response = await fetch('/api/favorites/ids/', {
      headers: {
        'Authorization': `Bearer ${authStore.token}`
      }
    })
    if (response.ok) {
      const data = await response.json()
      favoriteIds.value = new Set(data.questionIds || [])
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
  }
}

const toggleBookmark = async () => {
  if (!currentQuestion.value) return
  
  const questionId = currentQuestion.value.id
  const isCurrentlyFavorite = favoriteIds.value.has(questionId)
  
  try {
    if (isCurrentlyFavorite) {
      await removeFavorite(questionId)
      favoriteIds.value.delete(questionId)
    } else {
      await addFavorite(questionId)
      favoriteIds.value.add(questionId)
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    alert('操作失败，请稍后重试')
  }
}

const getOptionClass = (index) => {
  if (!showAnswer.value) {
    if (currentQuestion.value?.type === 'multiple') {
      return isSelected(index)
        ? 'border-primary-500 bg-primary-50'
        : 'border-gray-200 hover:border-gray-300'
    } else {
    return selectedOption.value === index
      ? 'border-primary-500 bg-primary-50'
      : 'border-gray-200 hover:border-gray-300'
  }
  }
  
  // 显示答案时
  const correctAnswers = currentQuestion.value.type === 'multiple' 
    ? (currentQuestion.value.correctAnswers || [currentQuestion.value.correctAnswer])
    : [currentQuestion.value.correctAnswer]
  
  if (correctAnswers.includes(index)) {
    return 'border-green-500 bg-green-50'
  }
  
  if (isSelected(index) && !correctAnswers.includes(index)) {
    return 'border-red-500 bg-red-50'
  }
  
  return 'border-gray-200'
}

const getCheckboxClass = (index) => {
  if (!showAnswer.value) {
    return isSelected(index)
      ? 'border-primary-500 bg-primary-500'
      : 'border-gray-300'
  }
  
  const correctAnswers = currentQuestion.value.correctAnswers || [currentQuestion.value.correctAnswer]
  if (correctAnswers.includes(index)) {
    return 'border-green-500 bg-green-500'
  }
  
  if (isSelected(index) && !correctAnswers.includes(index)) {
    return 'border-red-500 bg-red-500'
  }
  
  return 'border-gray-300'
}

const getOptionCircleClass = (index) => {
  if (!showAnswer.value) {
    return selectedOption.value === index ? 'border-primary-500' : 'border-gray-300'
  }
  
  const correctAnswers = currentQuestion.value.type === 'multiple' 
    ? (currentQuestion.value.correctAnswers || [currentQuestion.value.correctAnswer])
    : [currentQuestion.value.correctAnswer]
  
  if (correctAnswers.includes(index)) {
    return 'border-green-500'
  }
  
  if (selectedOption.value === index && !correctAnswers.includes(index)) {
    return 'border-red-500'
  }
  
  return 'border-gray-300'
}

const getOptionDotClass = (index) => {
  if (!showAnswer.value) {
    return selectedOption.value === index ? 'bg-primary-500' : ''
  }
  
  const correctAnswers = currentQuestion.value.type === 'multiple' 
    ? (currentQuestion.value.correctAnswers || [currentQuestion.value.correctAnswer])
    : [currentQuestion.value.correctAnswer]
  
  if (correctAnswers.includes(index)) {
    return 'bg-green-500'
  }
  
  if (selectedOption.value === index && !correctAnswers.includes(index)) {
    return 'bg-red-500'
  }
  
  return ''
}

const getTypeName = (type) => {
  if (type === 'single') return '单选题'
  if (type === 'multiple') return '多选题'
  if (type === 'judge') return '判断题'
  return type
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>