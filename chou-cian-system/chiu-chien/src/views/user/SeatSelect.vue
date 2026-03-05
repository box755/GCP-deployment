<template>
  <div class="seat-select">
    <div class="container">
      <el-card class="select-card">
        <template #header>
          <div class="card-header">
            <h2>選擇座位</h2>
            <div v-if="sessionUser" class="user-info">
              <span>{{ sessionUser.name }}</span>
            </div>
          </div>
        </template>

        <el-loading v-loading="loading" class="loading-container">
          <div v-if="!sessionUser" class="no-user-notice">
            <el-result
                icon="warning"
                title="您尚未加入此活動"
                sub-title="請先填寫資料加入活動"
            >
              <template #extra>
                <el-button
                    type="primary"
                    @click="goToJoin"
                    :size="buttonSize"
                    class="action-button"
                >
                  前往加入
                </el-button>
              </template>
            </el-result>
          </div>

          <div v-else-if="activity" class="content">
            <!-- 時間信息 -->
            <div class="time-info">
              <el-alert
                  :title="getTimeStatus()"
                  :type="getTimeStatusType()"
                  show-icon
                  :closable="false"
              />
            </div>

            <!-- 當前選擇信息 -->
            <div v-if="userSelection" class="current-selection">
              <el-card class="selection-card">
                <template #header>
                  <span class="selection-header">您當前的選擇</span>
                </template>
                <div class="selection-info">
                  <div class="selection-detail">
                    <strong>座位：{{ getCurrentSeatLabel() }}</strong>
                    <p>選擇時間：{{ formatDate(userSelection.selectionTime) }}</p>
                  </div>
                  <div class="selection-status">
                    <el-tag
                        :type="userSelection.status === 'confirmed' ? 'success' : 'warning'"
                        :size="tagSize"
                    >
                      {{ getSelectionStatusText(userSelection.status) }}
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 座位圖 -->
            <div class="seat-map-section">
              <h3>請選擇您想要的座位</h3>
              <div class="seat-map-wrapper">
                <SeatMap
                    :seats="seats"
                    :selections="allSelections"
                    :interactive="isSelectionActive"
                    :selected-seat="selectedSeatId"
                    @seat-click="onSeatClick"
                />
              </div>

              <!-- 座位圖例 -->
              <div class="seat-legend">
                <div class="legend-item">
                  <div class="legend-color available"></div>
                  <span>可選</span>
                </div>
                <div class="legend-item">
                  <div class="legend-color selected"></div>
                  <span>已選</span>
                </div>
                <div class="legend-item">
                  <div class="legend-color occupied"></div>
                  <span>佔用</span>
                </div>
              </div>
            </div>

            <!-- 操作按鈕 -->
            <div v-if="isSelectionActive" class="actions">
              <el-button
                  v-if="selectedSeatId"
                  type="primary"
                  @click="confirmSelection"
                  :loading="selectLoading"
                  :size="buttonSize"
                  class="action-button primary-action"
              >
                <span class="button-text">
                  確認選擇座位 {{ getSelectedSeatLabel() }}
                </span>
              </el-button>

              <el-button
                  v-if="userSelection"
                  type="warning"
                  @click="cancelSelection"
                  :loading="selectLoading"
                  :size="buttonSize"
                  class="action-button secondary-action"
              >
                取消當前選擇
              </el-button>
            </div>

            <!-- 選位結束後的說明 -->
            <div v-if="!isSelectionActive && activity.status !== 'completed'" class="end-notice">
              <el-card class="notice-card">
                <el-result
                    icon="info"
                    title="選位時間已結束"
                    sub-title="請等待管理員進行抽籤，結果將由管理員統一公布"
                />
              </el-card>
            </div>

            <!-- 活動已完成 -->
            <div v-if="activity.status === 'completed'" class="completed-notice">
              <el-card class="notice-card">
                <el-result
                    icon="success"
                    title="抽籤已完成"
                    sub-title="結果將由管理員統一公布"
                />
              </el-card>
            </div>
          </div>
        </el-loading>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import SeatMap from '@/components/SeatMap.vue'
import api from '@/api'

export default {
  name: 'UserSeatSelect',
  components: {
    SeatMap
  },
  props: {
    activityId: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const router = useRouter()
    const activityStore = useActivityStore()
    const userStore = useUserStore()

    // 從 sessionStorage 獲取會話用戶
    const sessionUser = ref(JSON.parse(sessionStorage.getItem(`activity_${props.activityId}_user`)))

    const seats = ref([])
    const allSelections = ref([])
    const selectedSeatId = ref(null)
    const selectLoading = ref(false)
    const refreshTimer = ref(null)

    const activity = computed(() => activityStore.currentActivity)
    const loading = computed(() => activityStore.loading || userStore.loading)
    const userSelection = computed(() => userStore.userSelection)

    // 響應式計算屬性
    const buttonSize = computed(() => {
      if (window.innerWidth < 480) return 'default'
      if (window.innerWidth < 768) return 'large'
      return 'large'
    })

    const tagSize = computed(() => {
      if (window.innerWidth < 480) return 'small'
      return 'default'
    })

    const isSelectionActive = computed(() => {
      if (!activity.value) return false
      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)
      return now >= startTime && now <= endTime && activity.value.status === 'active'
    })

    const getTimeStatus = () => {
      if (!activity.value) return ''
      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)

      if (now < startTime) {
        return `選位將於 ${formatDate(activity.value.selectionStartTime)} 開始`
      } else if (now > endTime) {
        return '選位時間已結束，等待抽籤結果'
      } else {
        return `選位進行中，將於 ${formatDate(activity.value.selectionEndTime)} 結束`
      }
    }

    const getTimeStatusType = () => {
      if (!activity.value) return 'info'
      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)

      if (now < startTime) {
        return 'warning'
      } else if (now > endTime) {
        return 'error'
      } else {
        return 'success'
      }
    }

    const getCurrentSeatLabel = () => {
      if (!userSelection.value) return ''
      const seat = seats.value.find(s => s.seatId === userSelection.value.seatId)
      return seat ? seat.label : userSelection.value.seatId
    }

    const getSelectedSeatLabel = () => {
      if (!selectedSeatId.value) return ''
      const seat = seats.value.find(s => s.seatId === selectedSeatId.value)
      return seat ? seat.label : selectedSeatId.value
    }

    const getSelectionStatusText = (status) => {
      const statusMap = {
        'pending': '待確認',
        'confirmed': '已確認',
        'conflicted': '存在衝突'
      }
      return statusMap[status] || status
    }

    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-TW')
    }

    const fetchSeats = async () => {
      try {
        const response = await api.getSeats(props.activityId)
        seats.value = response.data
      } catch (error) {
        console.error('獲取座位失敗:', error)
      }
    }

    const fetchSelections = async () => {
      try {
        const response = await api.getSeatSelections(props.activityId)
        allSelections.value = response.data
      } catch (error) {
        console.error('獲取選座記錄失敗:', error)
      }
    }

    const fetchUserSelection = async () => {
      if (!sessionUser.value) return
      try {
        await userStore.getUserSelection(props.activityId, sessionUser.value.id)
      } catch (error) {
        console.error('獲取用戶選座記錄失敗:', error)
      }
    }

    const onSeatClick = (seat) => {
      if (!isSelectionActive.value) {
        ElMessage.warning('選位時間已結束')
        return
      }

      if (!seat.available) {
        ElMessage.warning('此座位不可選')
        return
      }

      const seatSelections = allSelections.value.filter(s => s.seatId === seat.seatId)
      if (seatSelections.length > 0 && !seatSelections.some(s => s.userId === sessionUser.value?.id)) {
        const names = seatSelections.map(s => s.userName).join(', ')
        ElMessage.warning(`此座位已被 ${names} 選擇`)
      }

      selectedSeatId.value = selectedSeatId.value === seat.seatId ? null : seat.seatId
    }

    const confirmSelection = async () => {
      if (!selectedSeatId.value || !sessionUser.value) return

      try {
        await ElMessageBox.confirm(
            `確定要選擇座位 ${getSelectedSeatLabel()} 嗎？`,
            '確認選座',
            {
              confirmButtonText: '確定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        selectLoading.value = true
        await userStore.selectSeat(props.activityId, selectedSeatId.value, sessionUser.value.id)
        ElMessage.success('選座成功')
        selectedSeatId.value = null
        await fetchSelections()
        await fetchUserSelection()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('選座失敗，請稍後重試')
        }
      } finally {
        selectLoading.value = false
      }
    }

    const cancelSelection = async () => {
      if (!userSelection.value) return

      try {
        await ElMessageBox.confirm(
            '確定要取消當前的座位選擇嗎？',
            '取消選座',
            {
              confirmButtonText: '確定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        selectLoading.value = true
        await api.cancelSeatSelection(props.activityId, sessionUser.value.id)
        ElMessage.success('已取消選座')
        userStore.userSelection = null
        await fetchSelections()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消失敗，請稍後重試')
        }
      } finally {
        selectLoading.value = false
      }
    }

    const goToJoin = () => {
      router.push(`/activity/${props.activityId}`)
    }

    const startAutoRefresh = () => {
      refreshTimer.value = setInterval(() => {
        if (isSelectionActive.value) {
          fetchSelections()
        }
      }, 3000)
    }

    const stopAutoRefresh = () => {
      if (refreshTimer.value) {
        clearInterval(refreshTimer.value)
        refreshTimer.value = null
      }
    }

    onMounted(async () => {
      await activityStore.fetchActivity(props.activityId)
      await Promise.all([
        fetchSeats(),
        fetchSelections()
      ])

      if (sessionUser.value) {
        await fetchUserSelection()
      }

      startAutoRefresh()
    })

    onUnmounted(() => {
      stopAutoRefresh()
    })

    return {
      activity,
      loading,
      sessionUser,
      userSelection,
      seats,
      allSelections,
      selectedSeatId,
      selectLoading,
      buttonSize,
      tagSize,
      isSelectionActive,
      getTimeStatus,
      getTimeStatusType,
      getCurrentSeatLabel,
      getSelectedSeatLabel,
      getSelectionStatusText,
      formatDate,
      onSeatClick,
      confirmSelection,
      cancelSelection,
      goToJoin
    }
  }
}
</script>

<style scoped>
.seat-select {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.select-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.user-info {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.loading-container {
  min-height: 400px;
}

.no-user-notice {
  padding: 40px 20px;
}

.time-info {
  margin-bottom: 30px;
}

.current-selection {
  margin-bottom: 30px;
}

.selection-card {
  border: 2px solid #409eff;
}

.selection-header {
  font-weight: 600;
  color: #303133;
}

.selection-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 15px;
}

.selection-detail strong {
  font-size: 16px;
  color: #303133;
  display: block;
  margin-bottom: 8px;
}

.selection-detail p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.selection-status {
  flex-shrink: 0;
}

.seat-map-section {
  margin-bottom: 30px;
}

.seat-map-section h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
  font-size: 18px;
}

.seat-map-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  overflow-x: auto;
  padding: 10px;
}

.seat-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-top: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  border: 1px solid #ddd;
}

.legend-color.available {
  background-color: #f0f9ff;
  border-color: #67c23a;
}

.legend-color.selected {
  background-color: #409eff;
  border-color: #409eff;
}

.legend-color.occupied {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
  flex-wrap: wrap;
}

.action-button {
  padding: 12px 24px;
  font-weight: 500;
}

.primary-action {
  min-width: 200px;
}

.secondary-action {
  min-width: 150px;
}

.button-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.end-notice,
.completed-notice {
  margin-top: 30px;
}

.notice-card {
  background-color: #f8f9fa;
}

/* 平板樣式 */
@media (max-width: 768px) {
  .seat-select {
    padding: 15px;
  }

  .card-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 8px;
  }

  .card-header h2 {
    font-size: 20px;
  }

  .user-info {
    font-size: 15px;
  }

  .seat-map-section h3 {
    font-size: 16px;
  }

  .selection-info {
    flex-direction: column;
    gap: 12px;
  }

  .selection-detail strong {
    font-size: 15px;
  }

  .selection-detail p {
    font-size: 13px;
  }

  .actions {
    gap: 12px;
  }

  .primary-action {
    min-width: 180px;
  }

  .secondary-action {
    min-width: 130px;
  }

  .seat-legend {
    gap: 15px;
    padding: 12px;
  }

  .legend-item span {
    font-size: 14px;
  }
}

/* 手機樣式 */
@media (max-width: 480px) {
  .seat-select {
    padding: 10px;
  }

  .container {
    max-width: 100%;
  }

  .card-header h2 {
    font-size: 18px;
  }

  .user-info {
    font-size: 14px;
  }

  .time-info {
    margin-bottom: 20px;
  }

  .current-selection {
    margin-bottom: 20px;
  }

  .seat-map-section {
    margin-bottom: 20px;
  }

  .seat-map-section h3 {
    font-size: 15px;
    margin-bottom: 15px;
  }

  .seat-map-wrapper {
    padding: 5px;
  }

  .selection-detail strong {
    font-size: 14px;
    margin-bottom: 6px;
  }

  .selection-detail p {
    font-size: 12px;
  }

  .actions {
    flex-direction: column;
    align-items: center;
    gap: 10px;
    margin-top: 20px;
  }

  .action-button {
    width: 100%;
    max-width: 280px;
    padding: 12px 20px;
    font-size: 15px;
  }

  .primary-action,
  .secondary-action {
    min-width: auto;
  }

  .button-text {
    font-size: 14px;
  }

  .seat-legend {
    gap: 12px;
    padding: 10px;
    margin-top: 10px;
  }

  .legend-item {
    gap: 6px;
  }

  .legend-item span {
    font-size: 12px;
  }

  .legend-color {
    width: 14px;
    height: 14px;
  }

  .no-user-notice {
    padding: 30px 15px;
  }

  .end-notice,
  .completed-notice {
    margin-top: 20px;
  }
}

/* 小手機樣式 */
@media (max-width: 360px) {
  .seat-select {
    padding: 8px;
  }

  .card-header h2 {
    font-size: 16px;
  }

  .user-info {
    font-size: 13px;
  }

  .seat-map-section h3 {
    font-size: 14px;
  }

  .selection-detail strong {
    font-size: 13px;
  }

  .selection-detail p {
    font-size: 11px;
  }

  .action-button {
    max-width: 100%;
    padding: 10px 16px;
    font-size: 14px;
  }

  .button-text {
    font-size: 13px;
  }

  .seat-legend {
    gap: 10px;
    padding: 8px;
  }

  .legend-item span {
    font-size: 11px;
  }

  .legend-color {
    width: 12px;
    height: 12px;
  }
}
</style>
