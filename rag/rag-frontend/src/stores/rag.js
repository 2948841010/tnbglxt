import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as ragApi from '@/api/rag'

export const useRagStore = defineStore('rag', () => {
  // 状态
  const healthStatus = ref({ status: 'unknown' })
  const stats = ref({})
  const searchResults = ref([])
  const documents = ref([])
  const loading = ref(false)
  const searchLoading = ref(false)

  // 获取健康状态
  const fetchHealth = async () => {
    try {
      const { data } = await ragApi.getHealth()
      healthStatus.value = data
    } catch (error) {
      healthStatus.value = { status: 'unhealthy', error: error.message }
    }
  }

  // 获取统计信息
  const fetchStats = async () => {
    try {
      loading.value = true
      const { data } = await ragApi.getStats()
      stats.value = data
    } catch (error) {
      console.error('获取统计失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索知识
  const search = async (query, options = {}) => {
    try {
      searchLoading.value = true
      const params = {
        query,
        top_k: options.topK || 5,
        similarity_threshold: options.threshold || 0.0,
        use_cache: options.useCache !== false,
        include_entities: options.includeEntities !== false
      }
      const { data } = await ragApi.searchKnowledge(params)
      searchResults.value = data.results || []
      return data
    } catch (error) {
      console.error('搜索失败:', error)
      searchResults.value = []
      throw error
    } finally {
      searchLoading.value = false
    }
  }

  // 清理缓存
  const clearCache = async () => {
    try {
      const { data } = await ragApi.clearCache()
      return data
    } catch (error) {
      console.error('清理缓存失败:', error)
      throw error
    }
  }

  // 获取文档列表
  const fetchDocuments = async () => {
    try {
      loading.value = true
      const { data } = await ragApi.getDocuments()
      documents.value = data.documents || []
    } catch (error) {
      console.error('获取文档失败:', error)
      documents.value = []
    } finally {
      loading.value = false
    }
  }

  return {
    healthStatus,
    stats,
    searchResults,
    documents,
    loading,
    searchLoading,
    fetchHealth,
    fetchStats,
    search,
    clearCache,
    fetchDocuments
  }
})
