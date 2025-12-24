<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">题库管理</h1>
        <p class="text-gray-600 mt-1">管理系统题库，新增、编辑和删除题目</p>
      </div>
      <button @click="showAddModal = true" class="btn-primary">
        <svg class="h-5 w-5 inline-block mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        添加题目
      </button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <input
            v-model="filters.search"
            type="text"
            class="input-field"
            placeholder="搜索题目内容..."
          />
        </div>
        <div>
          <select v-model="filters.type" class="input-field">
            <option value="">全部类型</option>
            <option value="single">单选题</option>
            <option value="multiple">多选题</option>
            <option value="judge">判断题</option>
          </select>
        </div>
        <div>
          <select v-model="filters.difficulty" class="input-field">
            <option value="">全部难度</option>
            <option value="easy">简单</option>
            <option value="medium">中等</option>
            <option value="hard">困难</option>
          </select>
        </div>
        <div>
          <button @click="searchQuestions" class="w-full btn-primary">
            搜索
          </button>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="card">
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
                创建时间
              </th>
              <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="question in questions" :key="question.id" class="hover:bg-gray-50">
              <td class="px-6 py-4">
                <div class="text-sm text-gray-900 max-w-md truncate">{{ question.question }}</div>
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
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ question.createdAt }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                <button @click="viewQuestion(question)" class="text-primary-600 hover:text-primary-900">
                  查看
                </button>
                <button @click="editQuestion(question)" class="text-blue-600 hover:text-blue-900">
                  编辑
                </button>
                <button @click="deleteQuestion(question.id)" class="text-red-600 hover:text-red-900">
                  删除
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
            @click="currentPage--"
            :disabled="currentPage === 1"
            class="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            上一页
          </button>
          <button
            @click="currentPage++"
            :disabled="currentPage * pageSize >= totalCount"
            class="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑题目模态框 -->
    <div v-if="showAddModal || editingQuestion" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-lg max-w-3xl w-full max-h-[90vh] overflow-y-auto">
        <div class="p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-900">
              {{ editingQuestion ? '编辑题目' : '添加题目' }}
            </h2>
            <button @click="closeModal" class="text-gray-400 hover:text-gray-600">
              <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <form @submit.prevent="saveQuestion" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">题目类型</label>
              <select v-model="questionForm.type" class="input-field" required @change="onTypeChange">
                <option value="single">单选题</option>
                <option value="multiple">多选题</option>
                <option value="judge">判断题</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">难度</label>
              <select v-model="questionForm.difficulty" class="input-field" required>
                <option value="easy">简单</option>
                <option value="medium">中等</option>
                <option value="hard">困难</option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">题目内容</label>
              <textarea
                v-model="questionForm.question"
                rows="4"
                class="input-field"
                placeholder="请输入题目内容"
                required
              ></textarea>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">题目图片（可选）</label>
              <div class="space-y-3">
                <div v-if="questionForm.image" class="relative inline-block">
                  <img :src="getImageUrl(questionForm.image)" alt="题目图片" class="max-w-full h-auto max-h-64 rounded-lg border border-gray-300" />
                  <button
                    @click="questionForm.image = null"
                    type="button"
                    class="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
                  >
                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <div>
                  <input
                    type="file"
                    ref="imageInput"
                    @change="handleImageUpload"
                    accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                    class="hidden"
                  />
                  <button
                    @click="imageInput.click()"
                    type="button"
                    class="btn-secondary"
                    :disabled="imageUploading"
                  >
                    <svg class="h-5 w-5 inline-block mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {{ imageUploading ? '上传中...' : (questionForm.image ? '更换图片' : '上传图片') }}
                  </button>
                  <p class="text-xs text-gray-500 mt-1">支持 JPG、PNG、GIF、WebP 格式，最大 10MB</p>
                </div>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">选项</label>
              <div class="space-y-3">
                <div v-for="(option, index) in questionForm.options" :key="index" class="flex items-center space-x-2">
                  <span class="font-medium text-gray-700">{{ String.fromCharCode(65 + index) }}.</span>
                  <input
                    v-model="questionForm.options[index]"
                    type="text"
                    class="input-field flex-1"
                    :placeholder="`选项 ${String.fromCharCode(65 + index)}`"
                    required
                  />
                  <button
                    v-if="questionForm.options.length > 2"
                    @click="removeOption(index)"
                    type="button"
                    class="text-red-600 hover:text-red-800"
                  >
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                <button
                  v-if="questionForm.options.length < 6"
                  @click="addOption"
                  type="button"
                  class="text-primary-600 hover:text-primary-700 text-sm font-medium"
                >
                  + 添加选项
                </button>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">正确答案</label>
              <div v-if="questionForm.type === 'multiple'" class="space-y-2">
                <div
                  v-for="(option, index) in questionForm.options"
                  :key="index"
                  class="flex items-center space-x-2"
                >
                  <input
                    type="checkbox"
                    :value="index"
                    v-model="questionForm.correctAnswers"
                    class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                  />
                  <label class="text-sm text-gray-700">
                    {{ String.fromCharCode(65 + index) }}. {{ option }}
                  </label>
                </div>
                <p v-if="questionForm.correctAnswers.length === 0" class="text-sm text-red-600">
                  请至少选择一个正确答案
                </p>
              </div>
              <select v-else v-model="questionForm.correctAnswer" class="input-field" required>
                <option v-for="(option, index) in questionForm.options" :key="index" :value="index">
                  {{ String.fromCharCode(65 + index) }}. {{ option }}
                </option>
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">答案解析</label>
              <textarea
                v-model="questionForm.explanation"
                rows="3"
                class="input-field"
                placeholder="请输入答案解析"
                required
              ></textarea>
            </div>

            <div class="flex items-center justify-end space-x-4">
              <button type="button" @click="closeModal" class="btn-secondary">
                取消
              </button>
              <button type="submit" class="btn-primary">
                {{ editingQuestion ? '保存修改' : '添加题目' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 查看题目模态框 -->
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

          <div class="space-y-4">
            <div>
              <span class="text-sm font-medium text-gray-500">题目类型：</span>
              <span class="px-2 py-1 text-xs font-medium rounded-full ml-2" :class="getTypeClass(viewingQuestion.type)">
                {{ getTypeName(viewingQuestion.type) }}
              </span>
              <span class="px-2 py-1 text-xs font-medium rounded-full ml-2" :class="getDifficultyClass(viewingQuestion.difficulty)">
                {{ getDifficultyName(viewingQuestion.difficulty) }}
              </span>
            </div>

            <div>
              <h3 class="text-sm font-medium text-gray-500 mb-2">题目内容</h3>
              <p class="text-gray-900">{{ viewingQuestion.question }}</p>
              <div v-if="viewingQuestion.image" class="mt-3">
                <img :src="getImageUrl(viewingQuestion.image)" alt="题目图片" class="max-w-full h-auto max-h-96 rounded-lg border border-gray-300" />
              </div>
            </div>

            <div>
              <h3 class="text-sm font-medium text-gray-500 mb-2">选项</h3>
              <div class="space-y-2">
                <div
                  v-for="(option, index) in viewingQuestion.options"
                  :key="index"
                  class="p-3 rounded-lg border-2"
                  :class="index === viewingQuestion.correctAnswer ? 'border-green-500 bg-green-50' : 'border-gray-200'"
                >
                  <span class="font-medium mr-2">{{ String.fromCharCode(65 + index) }}.</span>
                  <span>{{ option }}</span>
                  <span v-if="index === viewingQuestion.correctAnswer" class="ml-auto float-right text-green-600 font-medium">
                    正确答案
                  </span>
                </div>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-medium text-gray-500 mb-2">答案解析</h3>
              <p class="text-gray-900 bg-blue-50 p-4 rounded-lg">{{ viewingQuestion.explanation }}</p>
            </div>
          </div>

          <div class="mt-6 flex justify-end">
            <button @click="viewingQuestion = null" class="btn-primary">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getQuestions, createQuestion, updateQuestion as apiUpdateQuestion, deleteQuestion as apiDeleteQuestion } from '@/api/questions'
import { uploadImage, getImageUrl } from '@/api/images'

const route = useRoute()

const questions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

const filters = reactive({
  search: '',
  type: '',
  difficulty: ''
})

const showAddModal = ref(false)
const editingQuestion = ref(null)
const viewingQuestion = ref(null)

const questionForm = reactive({
  type: 'single',
  difficulty: 'easy',
  question: '',
  image: null, // 图片URL
  options: ['', '', '', ''],
  correctAnswer: 0,
  correctAnswers: [], // 用于多选题
  explanation: ''
})

const imageInput = ref(null)
const imageUploading = ref(false)

// 加载数据的函数
const loadData = () => {
  loadQuestions()
}

onMounted(() => {
  loadData()
})

// 监听路由变化，每次进入页面时刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/admin/questions') {
    loadData()
  }
}, { immediate: false })

watch(currentPage, () => {
  loadQuestions()
})

const loadQuestions = async () => {
  try {
    const data = await getQuestions({
      page: currentPage.value,
      page_size: pageSize.value,
      ...filters
    })
    questions.value = data.results || []
    totalCount.value = data.count || 0
  } catch (error) {
    console.error('加载题目列表失败:', error)
    questions.value = []
    totalCount.value = 0
  }
}

const searchQuestions = () => {
  currentPage.value = 1
  loadQuestions()
}

const viewQuestion = (question) => {
  viewingQuestion.value = question
}

const editQuestion = (question) => {
  editingQuestion.value = question
  questionForm.type = question.type
  questionForm.difficulty = question.difficulty
  questionForm.question = question.question
  questionForm.image = question.image || null
  questionForm.options = [...question.options]
  if (question.type === 'multiple' && question.correctAnswers) {
    questionForm.correctAnswers = [...question.correctAnswers]
    questionForm.correctAnswer = question.correctAnswers[0] || 0
  } else {
  questionForm.correctAnswer = question.correctAnswer
    questionForm.correctAnswers = []
  }
  questionForm.explanation = question.explanation
}

const saveQuestion = async () => {
  try {
    // 验证多选题至少选择一个答案
    if (questionForm.type === 'multiple' && questionForm.correctAnswers.length === 0) {
      alert('多选题请至少选择一个正确答案')
      return
    }
    
    // 准备提交的数据
    const submitData = { ...questionForm }
    if (questionForm.type === 'multiple') {
      // 多选题：使用correctAnswers
      submitData.correctAnswers = questionForm.correctAnswers
      submitData.correctAnswer = questionForm.correctAnswers[0] || 0 // 向后兼容
    } else {
      // 单选题和判断题：使用correctAnswer
      submitData.correctAnswers = null
    }
    
    if (editingQuestion.value) {
      await apiUpdateQuestion(editingQuestion.value.id, submitData)
    } else {
      await createQuestion(submitData)
    }
    closeModal()
    loadQuestions()
  } catch (error) {
    alert('保存失败，请重试')
  }
}

const deleteQuestion = async (id) => {
  if (!confirm('确定要删除这道题目吗？')) return

  try {
    await apiDeleteQuestion(id)
    loadQuestions()
  } catch (error) {
    alert('删除失败，请重试')
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingQuestion.value = null
  questionForm.type = 'single'
  questionForm.difficulty = 'easy'
  questionForm.question = ''
  questionForm.image = null
  questionForm.options = ['', '', '', '']
  questionForm.correctAnswer = 0
  questionForm.correctAnswers = []
  questionForm.explanation = ''
}

const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 检查文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    alert('图片大小不能超过10MB')
    return
  }
  
  // 检查文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    alert('只支持 JPG、PNG、GIF、WebP 格式的图片')
    return
  }
  
  imageUploading.value = true
  try {
    const response = await uploadImage(file)
    questionForm.image = response.url || response.filename
  } catch (error) {
    console.error('图片上传失败:', error)
    alert('图片上传失败，请重试')
  } finally {
    imageUploading.value = false
    // 清空input，以便可以重复选择同一文件
    if (imageInput.value) {
      imageInput.value.value = ''
    }
  }
}

const onTypeChange = () => {
  // 当类型改变时，重置正确答案
  if (questionForm.type === 'multiple') {
    questionForm.correctAnswers = []
  } else {
    questionForm.correctAnswer = 0
    questionForm.correctAnswers = []
  }
}

const addOption = () => {
  questionForm.options.push('')
}

const removeOption = (index) => {
  questionForm.options.splice(index, 1)
  if (questionForm.correctAnswer >= questionForm.options.length) {
    questionForm.correctAnswer = 0
  }
}

const getTypeName = (type) => {
  if (type === 'single') return '单选题'
  if (type === 'multiple') return '多选题'
  if (type === 'judge') return '判断题'
  return type
}

const getTypeClass = (type) => {
  if (type === 'single') return 'bg-blue-100 text-blue-700'
  if (type === 'multiple') return 'bg-purple-100 text-purple-700'
  if (type === 'judge') return 'bg-green-100 text-green-700'
  return 'bg-gray-100 text-gray-700'
}

const getDifficultyName = (difficulty) => {
  const map = { easy: '简单', medium: '中等', hard: '困难' }
  return map[difficulty] || difficulty
}

const getDifficultyClass = (difficulty) => {
  const map = {
    easy: 'bg-green-100 text-green-700',
    medium: 'bg-yellow-100 text-yellow-700',
    hard: 'bg-red-100 text-red-700'
  }
  return map[difficulty] || ''
}
</script>
