import axios from 'axios'
import { ElMessage } from 'element-plus'

const apiClient = axios.create({
    baseURL: '/api',
    timeout: 10000
});

// 請求攔截器
apiClient.interceptors.request.use(
    config => {
        // 添加 token 到請求標頭
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }

        // 添加請求日誌（開發環境）
        if (process.env.NODE_ENV === 'development') {
            console.log('API 請求:', {
                method: config.method.toUpperCase(),
                url: config.url,
                data: config.data,
                headers: config.headers
            })
        }

        return config
    },
    error => {
        console.error('請求攔截器錯誤:', error)
        return Promise.reject(error)
    }
)

// 響應攔截器
apiClient.interceptors.response.use(
    response => {
        // 添加響應日誌（開發環境）
        if (process.env.NODE_ENV === 'development') {
            console.log('API 響應:', {
                status: response.status,
                url: response.config.url,
                data: response.data
            })
        }

        // 如果響應包含 data 屬性，則直接返回 data
        if (response.data && response.data.data !== undefined) {
            return {
                data: response.data.data,
                status: response.status,
                message: response.data.message
            }
        }
        return response
    },
    error => {
        console.error('API 響應錯誤:', error)

        // 處理網絡錯誤
        if (!error.response) {
            ElMessage.error('網絡連接失敗，請檢查網絡設置')
            return Promise.reject(new Error('網絡連接失敗'))
        }

        const status = error.response.status
        const message = error.response.data?.message || ''

        // ✅ 忽略「未找到選座記錄」的錯誤訊息
        if (message === '未找到選座記錄') {
            return Promise.reject(new Error(message))  // 呼叫端可自行判斷處理
        }

        // 處理 401 未授權錯誤（登入過期或未登入）
        if (error.response.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')

            // 如果不是登入頁面，則跳轉到登入
            if (window.location.pathname !== '/login') {
                ElMessage.warning('登入已過期，請重新登入')
                setTimeout(() => {
                    window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname)
                }, 1000)
            }
        }

        // 處理 403 權限錯誤
        else if (error.response.status === 403) {
            ElMessage.error('權限不足，無法執行此操作')
        }


        // 處理 404 錯誤
        else if (error.response.status === 404) {
            ElMessage.error('請求的資源不存在')

        }


        // 處理 500 服務器錯誤
        else if (error.response.status >= 500) {
            ElMessage.error('服務器錯誤，請稍後重試')
        }

        // 顯示其他錯誤消息
        else if (error.response.data && error.response.data.message) {
            ElMessage.error(error.response.data.message)
        } else {
            ElMessage.error('發生未知錯誤，請稍後重試')
        }

        // 包裝錯誤信息
        const apiError = new Error(error.response.data?.message || '請求失敗')
        apiError.status = error.response.status
        apiError.response = error.response

        return Promise.reject(apiError)
    }
)

const api = {
    // ================================
    // 認證相關 API
    // ================================

    /**
     * 管理員登入
     */
    login: (username, password) => {
        return apiClient.post('/auth/login', { username, password })
    },

    /**
     * 登出
     */
    logout: () => {
        // 清除本地存儲的令牌
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        return Promise.resolve({ data: true })
    },

    /**
     * 驗證當前 token 是否有效
     */
    validateToken: () => {
        return apiClient.get('/auth/validate')
    },

    // ================================
    // 活動管理相關 API
    // ================================

    /**
     * 獲取所有活動
     */
    getActivities: () => apiClient.get('/activities'),

    /**
     * 獲取單個活動詳情
     */
    getActivity: (id) => apiClient.get(`/activities/${id}`),

    /**
     * 創建新活動
     */
    createActivity: (data) => apiClient.post('/activities', data),

    /**
     * 更新活動信息
     */
    updateActivity: (id, data) => apiClient.put(`/activities/${id}`, data),

    /**
     * 刪除活動
     */
    deleteActivity: (id) => apiClient.delete(`/activities/${id}`),

    /**
     * 提前結束選位時間
     */
    endSelectionEarly: (activityId) => {
        return apiClient.put(`/activities/${activityId}/end-selection`)
    },

    /**
     * 更改活動狀態
     */
    updateActivityStatus: (activityId, status) => {
        return apiClient.put(`/activities/${activityId}/status`, { status })
    },

    resumeSelection:  (activityId) => {
        return apiClient.put(`/activities/${activityId}/resume-selection`)
    },

    // ================================
    // 用戶參與相關 API
    // ================================

    /**
     * 用戶加入活動
     */
    joinActivity: (activityId, userData) =>
        apiClient.post(`/activities/${activityId}/participants/join`, userData),

    /**
     * 用戶選座
     */
    selectSeat: (activityId, seatId, userId) => {
        console.log('API selectSeat 調用:', { activityId, seatId, userId })
        return apiClient.post(`/activities/${activityId}/seats/${seatId}/select`, { userId })
    },

    /**
     * 取消選座
     */
    cancelSeatSelection: (activityId, userId) =>
        apiClient.delete(`/activities/${activityId}/participants/${userId}/selection`),

    /**
     * 獲取用戶選座記錄
     */
    getUserSelection: (activityId, userId) =>
        apiClient.get(`/activities/${activityId}/participants/${userId}/selection`),

    // ================================
    // 座位相關 API
    // ================================

    /**
     * 獲取活動的所有座位
     */
    getSeats: (activityId) => apiClient.get(`/activities/${activityId}/seats`),

    /**
     * 獲取活動的選座記錄
     */
    getSeatSelections: (activityId) =>
        apiClient.get(`/activities/${activityId}/selections`),

    /**
     * 創建座位佈局
     */
    createSeatLayout: (activityId, seatLayoutData) =>
        apiClient.post(`/activities/${activityId}/seats`, seatLayoutData),

    /**
     * 更新座位信息
     */
    updateSeat: (activityId, seatId, seatData) =>
        apiClient.put(`/activities/${activityId}/seats/${seatId}`, seatData),

    /**
     * 重置座位可用性
     */
    resetSeatAvailability: (activityId) =>
        apiClient.put(`/activities/${activityId}/seats/reset`),

    // ================================
    // 參與者相關 API
    // ================================

    /**
     * 獲取活動參與者列表
     */
    getParticipants: (activityId) =>
        apiClient.get(`/activities/${activityId}/participants`),

    /**
     * 獲取特定參與者信息
     */
    getParticipant: (activityId, participantId) =>
        apiClient.get(`/activities/${activityId}/participants/${participantId}`),

    /**
     * 更新參與者信息
     */
    updateParticipant: (activityId, participantId, data) =>
        apiClient.put(`/activities/${activityId}/participants/${participantId}`, data),

    /**
     * 移除參與者
     */
    removeParticipant: (activityId, participantId) =>
        apiClient.delete(`/activities/${activityId}/participants/${participantId}`),

    // ================================
    // 抽籤相關 API
    // ================================

    /**
     * 執行抽籤
     */
    runLottery: (activityId) => {
        console.log('執行抽籤 API 調用:', { activityId })
        return apiClient.post(`/activities/${activityId}/lottery`)
    },

    /**
     * 獲取抽籤結果
     */
    getLotteryResult: (activityId) =>
        apiClient.get(`/activities/${activityId}/lottery/results`),

    /**
     * 重新抽籤
     */
    rerunLottery: (activityId) =>
        apiClient.post(`/activities/${activityId}/lottery/rerun`),

    /**
     * 手動分配座位
     */
    manualAssignSeat: (activityId, participantId, seatId) =>
        apiClient.post(`/activities/${activityId}/manual-assign`, {
            participantId,
            seatId
        }),

    // ================================
    // 統計和報告相關 API
    // ================================

    /**
     * 獲取活動統計數據
     */
    getActivityStats: (activityId) =>
        apiClient.get(`/activities/${activityId}/stats`),

    /**
     * 獲取衝突分析
     */
    getConflictAnalysis: (activityId) =>
        apiClient.get(`/activities/${activityId}/conflicts`),

    /**
     * 導出參與者數據
     */
    exportParticipants: (activityId, format = 'csv') =>
        apiClient.get(`/activities/${activityId}/export/participants?format=${format}`, {
            responseType: 'blob'
        }),
    getLotteryResults: async (activityId) => {
        return apiClient.get(`/activities/${activityId}/lottery/results`)
    },
    /**
     * 導出選座結果
     */
    exportResults: (activityId, format = 'csv') =>
        apiClient.get(`/activities/${activityId}/export/results?format=${format}`, {
            responseType: 'blob'
        }),

    // ================================
    // 系統相關 API
    // ================================

    /**
     * 獲取系統狀態
     */
    getSystemStatus: () => apiClient.get('/system/status'),

    /**
     * 清理過期活動
     */
    cleanupExpiredActivities: () => apiClient.post('/system/cleanup'),
}

// 導出 apiClient 實例，以便需要時直接使用
export { apiClient }

// 默認導出 api 對象
export default api
