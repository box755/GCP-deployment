<template>
  <div class="activity-detail">
    <div class="header">
      <div>
        <h2>{{ activity?.name || '活動詳情' }}</h2>
        <el-tag
            :type="getStatusType(activity?.status)"
            size="large"
        >
          {{ getStatusText(activity?.status) }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button @click="$router.go(-1)">返回</el-button>
      </div>
    </div>

    <el-loading v-loading="loading" class="loading-container">
      <div v-if="activity" class="content">
        <el-row :gutter="20">
          <el-col :span="16">
            <!-- 座位圖 -->
            <el-card class="seat-map-card">
              <template #header>
                <div class="card-header">
                  <span>座位分布圖</span>
                  <div class="actions">
                    <!-- 修正：分離的操作按鈕 -->
                    <el-button-group v-if="canShowActions">
                      <!-- 1. 活動進行中且選位時間未結束：提前結束選位 -->
                      <el-button
                          v-if="activity.status === 'active' && !isSelectionEnded"
                          type="warning"
                          @click="confirmEndSelection"
                          :loading="endSelectionLoading"
                      >
                        <el-icon><Clock /></el-icon>
                        提前結束選位
                      </el-button>

                      <!-- 2. 選位已結束狀態：可以恢復開始或進行抽籤 -->
                      <template v-if="activity.status === 'closed'">
                        <el-button
                            type="info"
                            @click="confirmResumeSelection"
                            :loading="resumeLoading"
                        >
                          <el-icon><RefreshLeft /></el-icon>
                          恢復選位
                        </el-button>

                        <el-button
                            type="primary"
                            @click="confirmLottery"
                            :loading="lotteryLoading"
                        >
                          <el-icon><Trophy /></el-icon>
                          開始抽籤
                        </el-button>
                      </template>

                      <!-- 3. 活動進行中且選位時間自然結束：正常抽籤 -->
                      <el-button
                          v-if="activity.status === 'active' && isSelectionEnded"
                          type="primary"
                          @click="confirmLottery"
                          :loading="lotteryLoading"
                      >
                        <el-icon><Trophy /></el-icon>
                        開始抽籤
                      </el-button>

                      <!-- 4. 活動已完成：查看結果 -->
                      <el-button
                          v-if="activity.status === 'completed'"
                          type="success"
                          @click="viewResults"
                      >
                        <el-icon><View /></el-icon>
                        查看最終結果
                      </el-button>
                    </el-button-group>

                    <!-- 活動狀態說明 -->
                    <div v-else class="status-explanation">
                      <el-text type="info" size="small">
                        {{ getStatusExplanation() }}
                      </el-text>
                    </div>
                  </div>
                </div>
              </template>

              <SeatMap
                  :seats="seats"
                  :selections="selections"
                  :show-names="true"
                  @seat-click="onSeatClick"
              />
            </el-card>
          </el-col>

          <el-col :span="8">
            <!-- 活動信息 -->
            <el-card class="info-card">
              <template #header>活動信息</template>

              <div class="info-list">
                <div class="info-item">
                  <strong>活動描述：</strong>
                  <p>{{ activity.description || '無描述' }}</p>
                </div>

                <div class="info-item">
                  <strong>選位時間：</strong>
                  <p>{{ formatDate(activity.selectionStartTime) }}</p>
                  <p>至 {{ formatDate(activity.selectionEndTime) }}</p>

                  <!-- 選位狀態提示 -->
                  <div class="time-status">
                    <el-alert
                        :title="getTimeStatusText()"
                        :type="getTimeStatusType()"
                        size="small"
                        :closable="false"
                        show-icon
                    />
                  </div>
                </div>

                <div class="info-item">
                  <strong>參與統計：</strong>
                  <p>總座位：{{ activity.totalSeats }} 個</p>
                  <p>已選座位：{{ selectedSeatsCount }} 個</p>
                  <p>參與人數：{{ participantCount }} 人</p>
                  <p>衝突座位：{{ conflictSeatsCount }} 個</p>

                  <!-- 選座進度 -->
                  <div class="selection-progress">
                    <el-progress
                        :percentage="selectionProgress.toFixed(3)"
                        :color="getProgressColor()"
                        :stroke-width="8"
                    />
                    <span class="progress-text">選座進度 {{ selectionProgress.toFixed(1) }}%</span>
                  </div>
                </div>

                <div class="info-item">
                  <strong>活動網址：</strong>
                  <el-input
                      :model-value="activityUrl"
                      readonly
                      class="url-input"
                  >
                    <template #append>
                      <el-button @click="copyUrl">複製</el-button>
                    </template>
                  </el-input>
                </div>
              </div>
            </el-card>

            <!-- 參與者列表保持不變 -->
            <el-card class="participants-card">
              <template #header>
                <div class="participants-header">
                  <span>參與者列表 ({{ participantCount }})</span>
                  <el-tooltip content="點擊參與者卡片查看詳細資訊" placement="top">
                    <el-icon><InfoFilled /></el-icon>
                  </el-tooltip>
                </div>
              </template>

              <div class="participants-list">
                <div
                    v-for="participant in participantsWithSeats"
                    :key="participant.id"
                    class="participant-card"
                    @click="showParticipantDetail(participant)"
                >
                  <div class="participant-main">
                    <div class="participant-info">
                      <div class="participant-name">
                        <strong>{{ participant.name }}</strong>
                        <el-tag
                            v-if="participant.selection"
                            :type="getSelectionStatusType(participant.selection)"
                            size="small"
                            class="status-tag"
                        >
                          {{ getSeatLabel(participant.selection.seatId) }}
                        </el-tag>
                        <el-tag v-else type="info" size="small" class="status-tag">
                          未選座
                        </el-tag>
                      </div>

                      <div class="participant-meta">
                        <div class="contact-info">
                          <el-icon class="contact-icon"><Message /></el-icon>
                          <span class="contact">{{ participant.contact || '未提供聯絡方式' }}</span>
                        </div>

                        <div class="join-info">
                          <el-icon class="time-icon"><Clock /></el-icon>
                          <span class="join-time">
                            {{ formatDate(participant.joinTime) }}
                          </span>
                        </div>
                      </div>
                    </div>

                    <!-- 選座詳情 -->
                    <div v-if="participant.selection" class="selection-detail">
                      <div class="selection-info">
                        <span class="selection-time">
                          選座時間：{{ formatDate(participant.selection.selectionTime) }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <!-- 操作按鈕 -->
                  <div class="participant-actions">
                    <el-tooltip content="複製聯絡方式" placement="top">
                      <el-button
                          size="small"
                          type="text"
                          @click.stop="copyContact(participant.contact)"
                          :disabled="!participant.contact"
                      >
                        <el-icon><CopyDocument /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </div>

                <!-- 空狀態 -->
                <div v-if="participantCount === 0" class="no-participants">
                  <el-empty description="尚無參與者" />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-loading>

    <el-dialog
        v-model="participantDialogVisible"
        title="參與者詳情"
        width="500px"
    >
      <div v-if="selectedParticipant" class="participant-detail-dialog">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">
            {{ selectedParticipant.name }}
          </el-descriptions-item>

          <el-descriptions-item label="聯絡方式">
            <div class="contact-display">
              {{ selectedParticipant.contact || '未提供' }}
              <el-button
                  v-if="selectedParticipant.contact"
                  size="small"
                  type="text"
                  @click="copyContact(selectedParticipant.contact)"
              >
                複製
              </el-button>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="加入時間">
            {{ formatDate(selectedParticipant.joinTime) }}
          </el-descriptions-item>

          <el-descriptions-item label="選座狀態">
            <el-tag
                v-if="selectedParticipant.selection"
                :type="getSelectionStatusType(selectedParticipant.selection)"
            >
              {{ getSeatLabel(selectedParticipant.selection.seatId) }}
            </el-tag>
            <el-tag v-else type="info">未選座</el-tag>
          </el-descriptions-item>

          <el-descriptions-item
              v-if="selectedParticipant.selection"
              label="選座時間"
          >
            {{ formatDate(selectedParticipant.selection.selectionTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import {
  Refresh,
  InfoFilled,
  Message,
  Clock,
  CopyDocument,
  Trophy,
  View,
  RefreshLeft
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SeatMap from '@/components/SeatMap.vue'
import api from '@/api'

export default {
  name: 'ActivityDetail',
  components: {
    SeatMap,
    Refresh,
    InfoFilled,
    Message,
    Clock,
    CopyDocument,
    Trophy,
    View,
    RefreshLeft
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const activityStore = useActivityStore()

    const seats = ref([])
    const selections = ref([])
    const participants = ref([])

    // 分離不同操作的 loading 狀態
    const endSelectionLoading = ref(false)
    const resumeLoading = ref(false)
    const lotteryLoading = ref(false)
    const refreshTimer = ref(null)

    const participantDialogVisible = ref(false)
    const selectedParticipant = ref(null)

    const activity = computed(() => activityStore.currentActivity)
    const loading = computed(() => activityStore.loading)

    const selectedSeatsCount = computed(() => {
      const uniqueSeats = new Set(selections.value.map(s => s.seatId))
      return uniqueSeats.size
    })

    const participantCount = computed(() =>
        participants.value.length
    )

    const conflictSeatsCount = computed(() => {
      const seatCounts = {}
      selections.value.forEach(selection => {
        seatCounts[selection.seatId] = (seatCounts[selection.seatId] || 0) + 1
      })
      return Object.values(seatCounts).filter(count => count > 1).length
    })

    const isSelectionEnded = computed(() => {
      if (!activity.value) return false
      const now = new Date()
      const endTime = new Date(activity.value.selectionEndTime)
      return now > endTime
    })

    // 判斷是否可以顯示操作按鈕
    const canShowActions = computed(() => {
      if (!activity.value) return false
      return ['active', 'closed', 'completed'].includes(activity.value.status)
    })

    const activityUrl = computed(() =>
        `${window.location.origin}/activity/${props.id}`
    )

    const participantsWithSeats = computed(() => {
      return participants.value.map(participant => {
        const selection = selections.value.find(s =>
            s.userId === participant.id || s.participantId === participant.id
        )

        return {
          ...participant,
          selection
        }
      })
    })

    const selectionProgress = computed(() => {
      if (!activity.value || participantCount.value === 0) return 0
      const selectedParticipants = participantsWithSeats.value.filter(p => p.selection).length
      return (selectedParticipants / participantCount.value) * 100
    })

    // 時間狀態相關
    const getTimeStatusText = () => {
      if (!activity.value) return ''
      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)

      if (activity.value.status === 'completed') {
        return '活動已完成，抽籤結果已確定'
      }

      if (activity.value.status === 'closed') {
        return '選位已提前結束，可進行抽籤或恢復選位'
      }

      if (now < startTime) {
        const timeDiff = startTime - now
        const hours = Math.floor(timeDiff / (1000 * 60 * 60))
        const minutes = Math.floor((timeDiff % (1000 * 60 * 60)) / (1000 * 60))
        return `選位將於 ${hours}小時${minutes}分鐘後開始`
      } else if (now > endTime) {
        return '選位時間已自然結束，可以開始抽籤'
      } else {
        const timeDiff = endTime - now
        const hours = Math.floor(timeDiff / (1000 * 60 * 60))
        const minutes = Math.floor((timeDiff % (1000 * 60 * 60)) / (1000 * 60))
        return `選位進行中，還有 ${hours}小時${minutes}分鐘結束`
      }
    }

    const getTimeStatusType = () => {
      if (!activity.value) return 'info'

      if (activity.value.status === 'completed') {
        return 'success'
      }

      if (activity.value.status === 'closed') {
        return 'warning'
      }

      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)

      if (now < startTime) {
        return 'warning'
      } else if (now > endTime) {
        return 'success'
      } else {
        return 'info'
      }
    }

    const getStatusExplanation = () => {
      if (!activity.value) return ''

      switch (activity.value.status) {
        case 'pending':
          return '活動尚未開始'
        case 'completed':
          return '活動已完成，查看最終結果'
        default:
          return ''
      }
    }

    const getProgressColor = () => {
      const progress = selectionProgress.value
      if (progress < 30) return '#f56c6c'
      if (progress < 70) return '#e6a23c'
      return '#67c23a'
    }

    const getStatusType = (status) => {
      const typeMap = {
        'active': 'success',
        'closed': 'warning',
        'completed': 'info',
        'pending': 'warning'
      }
      return typeMap[status] || 'info'
    }

    const getStatusText = (status) => {
      const textMap = {
        'active': '進行中',
        'closed': '選位已結束',
        'completed': '已完成',
        'pending': '待開始'
      }
      return textMap[status] || '未知'
    }

    const formatDate = (dateString) => {
      if (!dateString) return '未設定'
      const date = new Date(dateString)
      return date.toLocaleString('zh-TW')
    }

    const getSeatLabel = (seatId) => {
      if (!seatId) return ''
      const seat = seats.value.find(s => s.seatId === seatId)
      return seat ? seat.label : seatId
    }

    const getSelectionStatusType = (selection) => {
      if (!selection) return 'info'

      const conflictCount = selections.value.filter(s => s.seatId === selection.seatId).length

      if (conflictCount > 1) {
        return 'danger'
      } else if (selection.status === 'confirmed') {
        return 'success'
      } else {
        return 'warning'
      }
    }

    const copyUrl = async () => {
      try {
        await navigator.clipboard.writeText(activityUrl.value)
        ElMessage.success('網址已複製到剪貼板')
      } catch (error) {
        ElMessage.error('複製失敗')
      }
    }

    const copyContact = async (contact) => {
      if (!contact) {
        ElMessage.warning('該參與者未提供聯絡方式')
        return
      }

      try {
        await navigator.clipboard.writeText(contact)
        ElMessage.success('聯絡方式已複製到剪貼板')
      } catch (error) {
        ElMessage.error('複製失敗')
      }
    }

    const showParticipantDetail = (participant) => {
      selectedParticipant.value = participant
      participantDialogVisible.value = true
    }

    const fetchSeats = async () => {
      try {
        const response = await api.getSeats(props.id)
        seats.value = response.data
      } catch (error) {
        console.error('獲取座位失敗:', error)
      }
    }

    const fetchSelections = async () => {
      try {
        const response = await api.getSeatSelections(props.id)
        selections.value = response.data
      } catch (error) {
        console.error('獲取選座記錄失敗:', error)
      }
    }

    const fetchParticipants = async () => {
      try {
        const response = await api.getParticipants(props.id)
        participants.value = response.data
      } catch (error) {
        console.error('獲取參與者失敗:', error)
      }
    }

    const refreshData = async () => {
      await Promise.all([
        activityStore.fetchActivity(props.id),
        fetchSeats(),
        fetchSelections(),
        fetchParticipants()
      ])
    }

    // 1. 提前結束選位
    const confirmEndSelection = async () => {
      try {
        const selectedParticipants = participantsWithSeats.value.filter(p => p.selection).length
        const unselectedParticipants = participantCount.value - selectedParticipants

        let message = `確定要提前結束選位嗎？\n\n`
        message += `目前狀況：\n`
        message += `• 總參與者：${participantCount.value} 人\n`
        message += `• 已選座：${selectedParticipants} 人\n`
        message += `• 未選座：${unselectedParticipants} 人\n\n`

        if (unselectedParticipants > 0) {
          message += `⚠️ 注意：還有 ${unselectedParticipants} 位參與者未選座，\n`
          message += `提前結束後他們將無法再選座。\n\n`
        }

        if (conflictSeatsCount.value > 0) {
          message += `🔴 發現 ${conflictSeatsCount.value} 個座位有衝突。\n\n`
        }

        message += `提前結束後，您可以選擇進行抽籤或恢復選位。`

        await ElMessageBox.confirm(message, '提前結束選位', {
          confirmButtonText: '確定結束',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: false
        })

        await endSelectionEarly()

      } catch (error) {
        if (error !== 'cancel') {
          console.error('提前結束選位失敗:', error)
        }
      }
    }

    // 2. 恢復選位
    const confirmResumeSelection = async () => {
      try {
        await ElMessageBox.confirm(
            '確定要恢復選位嗎？選位時間將回到原定結束時間。',
            '恢復選位',
            {
              confirmButtonText: '確定恢復',
              cancelButtonText: '取消',
              type: 'info'
            }
        )

        await resumeSelection()

      } catch (error) {
        if (error !== 'cancel') {
          console.error('恢復選位失敗:', error)
        }
      }
    }

    // 3. 抽籤確認
    const confirmLottery = async () => {
      try {
        let message = '確定要開始抽籤嗎？\n\n'

        if (conflictSeatsCount.value > 0) {
          message += `🔴 發現 ${conflictSeatsCount.value} 個座位有衝突，抽籤將解決衝突。\n\n`
        }

        message += '⚠️ 抽籤後將無法恢復選位或重新抽籤。'

        await ElMessageBox.confirm(message, '確認抽籤', {
          confirmButtonText: '開始抽籤',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: false
        })

        await runLottery()

      } catch (error) {
        if (error !== 'cancel') {
          console.error('抽籤失敗:', error)
        }
      }
    }

    // API 調用方法
    const endSelectionEarly = async () => {
      endSelectionLoading.value = true

      try {
        ElMessage.info('正在結束選位時間...')
        await api.endSelectionEarly(props.id)

        ElMessage.success('選位已提前結束！')
        await refreshData()

      } catch (error) {
        console.error('提前結束選位失敗:', error)
        ElMessage.error(`提前結束選位失敗：${error.message || '未知錯誤'}`)
      } finally {
        endSelectionLoading.value = false
      }
    }

    const resumeSelection = async () => {
      resumeLoading.value = true

      try {
        ElMessage.info('正在恢復選位...')
        await api.resumeSelection(props.id)

        ElMessage.success('選位已恢復！')
        await refreshData()

      } catch (error) {
        console.error('恢復選位失敗:', error)
        ElMessage.error(`恢復選位失敗：${error.message || '未知錯誤'}`)
      } finally {
        resumeLoading.value = false
      }
    }

    const runLottery = async () => {
      lotteryLoading.value = true

      try {
        ElMessage.info('正在進行抽籤...')
        await api.runLottery(props.id)

        ElMessage.success('抽籤完成！')
        await refreshData()

      } catch (error) {
        console.error('抽籤失敗:', error)
        ElMessage.error(`抽籤失敗：${error.message || '未知錯誤'}`)
      } finally {
        lotteryLoading.value = false
      }
    }

    const viewResults = () => {
      router.push(`/admin/activity/${props.id}/result`)
    }

    const onSeatClick = (seat) => {
      const seatSelections = selections.value.filter(s => s.seatId === seat.seatId)

      if (seatSelections.length > 0) {
        const names = seatSelections.map(s => s.userName || s.participantName).join(', ')
        ElMessage.info(`座位 ${seat.label} 的選擇者：${names}`)
      } else {
        ElMessage.info(`座位 ${seat.label} 尚未被選擇`)
      }
    }

    const startAutoRefresh = () => {
      refreshTimer.value = setInterval(() => {
        // 在 active 和 closed 狀態下都需要刷新
        if (activity.value?.status === 'active' || activity.value?.status === 'closed') {
          fetchSelections()
          fetchParticipants()
        }
      }, 5000)
    }

    const stopAutoRefresh = () => {
      if (refreshTimer.value) {
        clearInterval(refreshTimer.value)
        refreshTimer.value = null
      }
    }

    onMounted(async () => {
      await refreshData()
      startAutoRefresh()
    })

    onUnmounted(() => {
      stopAutoRefresh()
    })

    return {
      activity,
      loading,
      seats,
      selections,
      participants,
      endSelectionLoading,
      resumeLoading,
      lotteryLoading,
      selectedSeatsCount,
      participantCount,
      conflictSeatsCount,
      isSelectionEnded,
      canShowActions,
      activityUrl,
      participantsWithSeats,
      participantDialogVisible,
      selectedParticipant,
      selectionProgress,
      getSeatLabel,
      getSelectionStatusType,
      getStatusType,
      getStatusText,
      getTimeStatusText,
      getTimeStatusType,
      getStatusExplanation,
      getProgressColor,
      formatDate,
      copyUrl,
      copyContact,
      showParticipantDetail,
      refreshData,
      confirmEndSelection,
      confirmResumeSelection,
      confirmLottery,
      viewResults,
      onSeatClick
    }
  }
}
</script>


<style scoped>
.activity-detail {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.header h2 {
  margin: 0 0 10px 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.loading-container {
  min-height: 400px;
}

.seat-map-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-explanation {
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.info-card,
.participants-card {
  margin-bottom: 20px;
}

.info-list {
  space-y: 15px;
}

.info-item {
  margin-bottom: 15px;
}

.info-item strong {
  color: #303133;
  margin-bottom: 5px;
  display: block;
}

.info-item p {
  margin: 5px 0;
  color: #606266;
}

.url-input {
  margin-top: 5px;
}

/* 新增：時間狀態和進度相關樣式 */
.time-status {
  margin-top: 10px;
}

.selection-progress {
  margin-top: 10px;
}

.progress-text {
  font-size: 12px;
  color: #606266;
  margin-top: 5px;
  display: block;
}

/* 參與者相關樣式 */
.participants-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.participants-list {
  max-height: 500px;
  overflow-y: auto;
}

.participant-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fff;
}

.participant-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-2px);
}

.participant-card:last-child {
  margin-bottom: 0;
}

.participant-main {
  flex: 1;
}

.participant-info {
  margin-bottom: 8px;
}

.participant-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.participant-name strong {
  font-size: 16px;
  color: #303133;
  margin: 0;
}

.status-tag {
  flex-shrink: 0;
}

.participant-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-info,
.join-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.contact-icon,
.time-icon {
  font-size: 14px;
  color: #909399;
}

.contact {
  color: #606266;
  font-weight: 500;
}

.join-time {
  color: #909399;
}

.selection-detail {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.selection-time {
  font-size: 12px;
  color: #909399;
}

.participant-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-self: center;
}

.no-participants {
  padding: 40px 0;
  text-align: center;
}

/* 彈窗樣式 */
.participant-detail-dialog {
  padding: 10px 0;
}

.contact-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .participant-card {
    flex-direction: column;
    gap: 12px;
  }

  .participant-actions {
    flex-direction: row;
    align-self: flex-start;
  }
}
</style>
