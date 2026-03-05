import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import api from '@/api'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
    // 狀態
    const user = ref(JSON.parse(localStorage.getItem('user')) || null)
    const token = ref(localStorage.getItem('token') || null)
    const currentUser = ref(null) // 用戶參與活動時的用戶
    const userSelection = ref(null)
    const loading = ref(false)
    const error = ref(null)

    // 監聽器設置本地存儲
    watch(user, (newUser) => {
        if (newUser) {
            localStorage.setItem('user', JSON.stringify(newUser))
        } else {
            localStorage.removeItem('user')
        }
    }, { deep: true })

    watch(token, (newToken) => {
        if (newToken) {
            localStorage.setItem('token', newToken)
        } else {
            localStorage.removeItem('token')
        }
    })

    // 登入相關操作
    const login = async (username, password) => {
        loading.value = true
        error.value = null
        try {
            // 這裡假設 API 返回 token 和用戶資訊
            const response = await api.login(username, password)
            token.value = response.data.token
            user.value = response.data.user
            return user.value
        } catch (err) {
            error.value = err.response?.data?.message || '登入失敗，請檢查您的用戶名和密碼'
            throw error.value
        } finally {
            loading.value = false
        }
    }

    const logout = async () => {
        loading.value = true
        try {
            // 可能需要呼叫伺服器進行登出操作
            await api.logout(token.value)
        } catch (err) {
            console.error('登出時發生錯誤:', err)
        } finally {
            // 清除本地狀態
            token.value = null
            user.value = null
            currentUser.value = null
            userSelection.value = null
            error.value = null
            loading.value = false
        }
    }

    const checkAuth = () => {
        return !!token.value
    }

    // 參與活動相關操作
    const joinActivity = async (activityId, userData) => {
        loading.value = true
        error.value = null
        try {
            const response = await api.joinActivity(activityId, userData)
            currentUser.value = response.data
            return response.data
        } catch (err) {
            error.value = err.message
            throw err
        } finally {
            loading.value = false
        }
    }

    const selectSeat = async (activityId, seatId, userId) => {
        loading.value = true
        error.value = null
        try {
            const response = await api.selectSeat(activityId, seatId, userId)
            userSelection.value = response.data
            return response.data
        } catch (err) {
            error.value = err.message
            throw err
        } finally {
            loading.value = false
        }
    }

    const getUserSelection = async (activityId, userId) => {
        loading.value = true
        error.value = null
        try {
            const response = await api.getUserSelection(activityId, userId)
            userSelection.value = response.data
            return response.data
        } catch (err) {
            error.value = err.message
            throw err
        } finally {
            loading.value = false
        }
    }

    const clearUser = () => {
        currentUser.value = null
        userSelection.value = null
        error.value = null
    }

    return {
        // 狀態
        user,
        token,
        currentUser,
        userSelection,
        loading,
        error,
        // 認證相關
        login,
        logout,
        checkAuth,
        // 參與活動相關
        joinActivity,
        selectSeat,
        getUserSelection,
        clearUser
    }
})
