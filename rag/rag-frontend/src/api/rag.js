import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

// 知识检索
export const searchKnowledge = (params) => {
  return api.post('/search', params)
}

// 健康检查
export const getHealth = () => {
  return api.get('/health')
}

// 获取统计信息
export const getStats = () => {
  return api.get('/stats')
}

// 清理缓存
export const clearCache = () => {
  return api.post('/cache/clear')
}

// 上传文档
export const uploadDocument = (formData) => {
  return api.post('/documents/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000 // 5分钟超时
  })
}

// 获取文档列表
export const getDocuments = () => {
  return api.get('/documents')
}

// 删除文档
export const deleteDocument = (id) => {
  return api.delete(`/documents/${id}`)
}

// 删除文档及其所有知识条目
export const deleteDocumentWithItems = (docId) => {
  return api.delete(`/documents/${docId}/all`)
}

// 重建索引
export const rebuildIndex = () => {
  return api.post('/index/rebuild')
}

// ============ 知识条目管理 ============

// 获取知识条目列表（分页）
export const getKnowledgeItems = (page = 1, pageSize = 20, docId = null) => {
  const params = { page, page_size: pageSize }
  if (docId) params.doc_id = docId
  return api.get('/knowledge/items', { params })
}

// 获取文档下的知识条目
export const getDocumentItems = (docId, page = 1, pageSize = 20) => {
  return api.get(`/documents/${docId}/items`, { params: { page, page_size: pageSize } })
}

// 获取单个知识条目
export const getKnowledgeItem = (itemId) => {
  return api.get(`/knowledge/items/${itemId}`)
}

// 创建知识条目
export const createKnowledgeItem = (data) => {
  return api.post('/knowledge/items', data)
}

// 更新知识条目
export const updateKnowledgeItem = (itemId, data) => {
  return api.put(`/knowledge/items/${itemId}`, data)
}

// 删除知识条目
export const deleteKnowledgeItem = (itemId) => {
  return api.delete(`/knowledge/items/${itemId}`)
}

// 批量删除知识条目
export const batchDeleteKnowledgeItems = (itemIds) => {
  return api.post('/knowledge/items/batch-delete', { item_ids: itemIds })
}

export default api
