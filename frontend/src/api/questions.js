import apiClient from './client'

// 获取题目列表
export const getQuestions = (params) => {
  return apiClient.get('/questions/', { params })
}

// 获取单个题目
export const getQuestion = (id) => {
  return apiClient.get(`/questions/${id}/`)
}

// 创建题目
export const createQuestion = (data) => {
  return apiClient.post('/questions/', data)
}

// 更新题目
export const updateQuestion = (id, data) => {
  return apiClient.put(`/questions/${id}/`, data)
}

// 删除题目
export const deleteQuestion = (id) => {
  return apiClient.delete(`/questions/${id}/`)
}

// 批量导入题目
export const importQuestions = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return apiClient.post('/questions/import/', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取随机题目
export const getRandomQuestions = (count, type) => {
  const params = { count }
  if (type) {
    params.type = type
  }
  return apiClient.get('/questions/random/', { params })
}

// 获取顺序题目
export const getSequentialQuestions = (params) => {
  return apiClient.get('/questions/sequential/', { params })
}
