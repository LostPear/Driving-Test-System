import apiClient from './client'

// 获取收藏列表
export const getFavorites = (params) => {
  return apiClient.get('/favorites/', { params })
}

// 添加收藏
export const addFavorite = (questionId) => {
  return apiClient.post('/favorites/', { question_id: questionId })
}

// 删除收藏
export const removeFavorite = (questionId) => {
  return apiClient.delete(`/favorites/${questionId}/`)
}

// 检查是否已收藏
export const checkFavorite = (questionId) => {
  return apiClient.get(`/favorites/check/${questionId}/`)
}

// 获取收藏的题目ID列表
export const getFavoriteIds = () => {
  return apiClient.get('/favorites/ids/')
}

