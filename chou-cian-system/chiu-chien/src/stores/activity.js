import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useActivityStore = defineStore('activity', () => {
    const activities = ref([])
    const currentActivity = ref(null)
    const loading = ref(false)
    const error = ref(null)

    // 修正：包含所有活動狀態的分類
    const activeActivities = computed(() =>
        activities.value.filter(activity =>
            activity.status === 'active' || activity.status === 'closed' // 包含 closed 狀態
        )
    )

    const completedActivities = computed(() =>
        activities.value.filter(activity => activity.status === 'completed')
    )

    const pendingActivities = computed(() =>
        activities.value.filter(activity => activity.status === 'pending')
    )

    // 新增：獲取不同狀態的活動數量
    const getActivityCountByStatus = computed(() => ({
        active: activities.value.filter(a => a.status === 'active').length,
        closed: activities.value.filter(a => a.status === 'closed').length,
        completed: activities.value.filter(a => a.status === 'completed').length,
        pending: activities.value.filter(a => a.status === 'pending').length,
        total: activities.value.length
    }))

    const fetchActivities = async () => {
        loading.value = true
        error.value = null

        try {
            console.log('開始獲取活動列表')
            const response = await api.getActivities()
            activities.value = response.data || []

            console.log('獲取到活動:', activities.value.length, '個')
            console.log('活動狀態分布:', getActivityCountByStatus.value)

        } catch (err) {
            console.error('獲取活動列表失敗:', err)
            error.value = err.message || '獲取活動列表失敗'
        } finally {
            loading.value = false
        }
    }

    const fetchActivity = async (id) => {
        loading.value = true
        error.value = null

        try {
            console.log('獲取活動詳情，ID:', id)
            const response = await api.getActivity(id)
            currentActivity.value = response.data

            // 同時更新活動列表中的對應項目
            const index = activities.value.findIndex(a => a.id === parseInt(id))
            if (index !== -1) {
                activities.value[index] = response.data
            }

            console.log('獲取活動詳情成功:', currentActivity.value)
            return currentActivity.value

        } catch (err) {
            console.error('獲取活動詳情失敗:', err)
            error.value = err.message || '獲取活動詳情失敗'
            throw err
        } finally {
            loading.value = false
        }
    }

    const createActivity = async (activityData) => {
        loading.value = true
        error.value = null

        try {
            console.log('創建活動:', activityData)
            const response = await api.createActivity(activityData)
            const newActivity = response.data

            activities.value.unshift(newActivity)
            console.log('活動創建成功:', newActivity)

            return newActivity
        } catch (err) {
            console.error('創建活動失敗:', err)
            error.value = err.message || '創建活動失敗'
            throw err
        } finally {
            loading.value = false
        }
    }

    const updateActivity = async (id, activityData) => {
        loading.value = true
        error.value = null

        try {
            console.log('更新活動:', { id, activityData })
            const response = await api.updateActivity(id, activityData)
            const updatedActivity = response.data

            const index = activities.value.findIndex(a => a.id === id)
            if (index !== -1) {
                activities.value[index] = updatedActivity
            }

            if (currentActivity.value && currentActivity.value.id === id) {
                currentActivity.value = updatedActivity
            }

            console.log('活動更新成功:', updatedActivity)
            return updatedActivity
        } catch (err) {
            console.error('更新活動失敗:', err)
            error.value = err.message || '更新活動失敗'
            throw err
        } finally {
            loading.value = false
        }
    }

    const deleteActivity = async (id) => {
        loading.value = true
        error.value = null

        try {
            console.log('刪除活動，ID:', id)
            await api.deleteActivity(id)

            activities.value = activities.value.filter(a => a.id !== id)
            if (currentActivity.value && currentActivity.value.id === id) {
                currentActivity.value = null
            }

            console.log('活動刪除成功')
        } catch (err) {
            console.error('刪除活動失敗:', err)
            error.value = err.message || '刪除活動失敗'
            throw err
        } finally {
            loading.value = false
        }
    }

    const clearError = () => {
        error.value = null
    }

    return {
        // 狀態
        activities,
        currentActivity,
        loading,
        error,

        // 計算屬性
        activeActivities,
        completedActivities,
        pendingActivities,
        getActivityCountByStatus,

        // 操作
        fetchActivities,
        fetchActivity,
        createActivity,
        updateActivity,
        deleteActivity,
        clearError
    }
})
