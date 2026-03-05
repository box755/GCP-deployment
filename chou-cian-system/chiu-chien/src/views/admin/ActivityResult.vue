<template>
  <div class="activity-result">
    <div class="header">
      <h2>{{ activity?.name || '活動' }} - 抽籤結果</h2>
      <div class="header-actions">
        <el-button @click="refreshResults" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新結果
        </el-button>
        <el-button @click="exportResults">
          <el-icon><Download /></el-icon>
          匯出結果
        </el-button>
        <el-button @click="$router.go(-1)">返回</el-button>
      </div>
    </div>

    <el-loading v-loading="loading" class="loading-container">
      <!-- 統計概覽 -->
      <el-card class="summary-card">
        <template #header>
          <h3>抽籤結果統計</h3>
        </template>

        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="總參與者" :value="summary.totalParticipants" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="成功分配" :value="summary.successfulAssignments" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="重新分配" :value="summary.reassignments" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="未分配" :value="summary.failedAssignments" />
          </el-col>
        </el-row>
      </el-card>

      <!-- 詳細結果表格 -->
      <el-card class="results-table">
        <template #header>
          <div class="table-header">
            <span>詳細抽籤結果</span>
            <div class="filters">
              <el-select v-model="filterType" placeholder="篩選類型" clearable>
                <el-option label="全部" value="" />
                <el-option label="原座位" value="original" />
                <el-option label="重新分配" value="reassigned" />
                <el-option label="未分配" value="no_seat" />
              </el-select>
            </div>
          </div>
        </template>

        <el-table :data="filteredResults" stripe>
          <el-table-column prop="participantName" label="參與者" width="120" />

          <el-table-column label="原始選擇" width="100">
            <template #default="{ row }">
              <el-tag type="info" size="small">{{ getSeatLabel(row.originalSeatId) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="最終分配" width="100">
            <template #default="{ row }">
              <el-tag
                  v-if="row.assignedSeatId"
                  :type="getAssignmentType(row)"
                  size="small"
              >
                {{ getSeatLabel(row.assignedSeatId) }}
              </el-tag>
              <el-tag v-else type="danger" size="small">無座位</el-tag>
            </template>
          </el-table-column>

          <el-table-column label="狀態" width="140">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row)">
                {{ getStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="衝突情況" width="100" align="center">
            <template #default="{ row }">
              <el-badge
                  v-if="row.conflictCount > 1"
                  :value="row.conflictCount"
                  type="danger"
              >
                <el-icon><Warning /></el-icon>
              </el-badge>
              <el-text v-else type="success">無衝突</el-text>
            </template>
          </el-table-column>

          <el-table-column prop="lotteryTime" label="抽籤時間" width="160">
            <template #default="{ row }">
              {{ formatDate(row.lotteryTime) }}
            </template>
          </el-table-column>

          <el-table-column label="說明" min-width="200">
            <template #default="{ row }">
              <span :class="getDescriptionClass(row)">
                {{ getDescription(row) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 座位分布圖 -->
      <el-card class="seat-map-card">
        <template #header>
          <h3>最終座位分布</h3>
        </template>

        <SeatMap
            :seats="seats"
            :selections="finalSelections"
            :show-names="true"
            :readonly="true"
        />

        <div class="seat-legend">
          <div class="legend-item">
            <div class="legend-color original"></div>
            <span>原始選擇</span>
          </div>
          <div class="legend-item">
            <div class="legend-color reassigned"></div>
            <span>重新分配</span>
          </div>
          <div class="legend-item">
            <div class="legend-color empty"></div>
            <span>空座位</span>
          </div>
        </div>
      </el-card>
    </el-loading>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import { Refresh, Download, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import SeatMap from '@/components/SeatMap.vue'
import api from '@/api'

export default {
  name: 'ActivityResult',
  components: {
    SeatMap,
    Refresh,
    Download,
    Warning
  },
  setup() {
    const route = useRoute()
    const activityStore = useActivityStore()
    const activityId = route.params.id

    const loading = ref(false)
    const results = ref([])
    const seats = ref([])
    const filterType = ref('')

    const activity = computed(() => activityStore.currentActivity)

    const summary = computed(() => {
      const total = results.value.length
      const successful = results.value.filter(r => r.assignedSeatId && r.assignedSeatId !== '').length
      const reassigned = results.value.filter(r => r.reassigned).length
      const failed = results.value.filter(r => !r.assignedSeatId || r.assignedSeatId === '').length

      return {
        totalParticipants: total,
        successfulAssignments: successful,
        reassignments: reassigned,
        failedAssignments: failed
      }
    })

    const filteredResults = computed(() => {
      if (!filterType.value) return results.value

      return results.value.filter(result => {
        switch (filterType.value) {
          case 'original':
            return !result.reassigned && result.assignedSeatId
          case 'reassigned':
            return result.reassigned
          case 'no_seat':
            return !result.assignedSeatId || result.assignedSeatId === ''
          default:
            return true
        }
      })
    })

    const finalSelections = computed(() => {
      return results.value
          .filter(r => r.assignedSeatId && r.assignedSeatId !== '')
          .map(r => ({
            seatId: r.assignedSeatId,
            userName: r.participantName,
            participantName: r.participantName,
            isReassigned: r.reassigned,
            status: 'confirmed'
          }))
    })

    const getSeatLabel = (seatId) => {
      if (!seatId) return '無'
      const seat = seats.value.find(s => s.seatId === seatId)
      return seat ? seat.label : seatId
    }

    const getAssignmentType = (result) => {
      if (!result.assignedSeatId) return 'danger'
      return result.reassigned ? 'warning' : 'success'
    }

    const getStatusType = (result) => {
      if (!result.assignedSeatId || result.assignedSeatId === '') {
        return 'danger'
      } else if (result.reassigned) {
        return 'warning'
      } else {
        return 'success'
      }
    }

    const getStatusText = (result) => {
      if (!result.assignedSeatId || result.assignedSeatId === '') {
        return '未分配座位'
      } else if (result.reassigned) {
        return '重新分配'
      } else if (result.conflictCount > 1) {
        return '衝突獲勝'
      } else {
        return '直接分配'
      }
    }

    const getDescription = (result) => {
      if (!result.assignedSeatId || result.assignedSeatId === '') {
        return '在衝突中失敗且無空座位可分配'
      } else if (result.reassigned) {
        return `從衝突座位 ${getSeatLabel(result.originalSeatId)} 重新分配到 ${getSeatLabel(result.assignedSeatId)}`
      } else if (result.conflictCount > 1) {
        return `在 ${result.conflictCount} 人競爭中獲勝，獲得座位 ${getSeatLabel(result.assignedSeatId)}`
      } else {
        return `成功獲得原始選擇的座位 ${getSeatLabel(result.assignedSeatId)}`
      }
    }

    const getDescriptionClass = (result) => {
      if (!result.assignedSeatId || result.assignedSeatId === '') {
        return 'text-danger'
      } else if (result.reassigned) {
        return 'text-warning'
      } else {
        return 'text-success'
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return '未設定'
      const date = new Date(dateString)
      return date.toLocaleString('zh-TW')
    }

    const fetchResults = async () => {
      loading.value = true
      try {
        const response = await api.getLotteryResults(activityId)
        results.value = response.data
        console.log('抽籤結果:', results.value)
      } catch (error) {
        console.error('獲取抽籤結果失敗:', error)
        ElMessage.error('獲取抽籤結果失敗')
      } finally {
        loading.value = false
      }
    }

    const fetchSeats = async () => {
      try {
        const response = await api.getSeats(activityId)
        seats.value = response.data
      } catch (error) {
        console.error('獲取座位失敗:', error)
      }
    }

    const refreshResults = async () => {
      await Promise.all([
        fetchResults(),
        fetchSeats(),
        activityStore.fetchActivity(activityId)
      ])
    }

    const exportResults = () => {
      const csvContent = generateCSV()
      downloadCSV(csvContent, `${activity.value?.name || '活動'}_抽籤結果.csv`)
    }

    const generateCSV = () => {
      const headers = ['參與者', '原始選擇', '最終分配', '狀態', '說明']
      const rows = results.value.map(result => [
        result.participantName,
        getSeatLabel(result.originalSeatId),
        result.assignedSeatId ? getSeatLabel(result.assignedSeatId) : '無座位',
        getStatusText(result),
        getDescription(result)
      ])

      return [headers, ...rows]
          .map(row => row.map(cell => `"${cell}"`).join(','))
          .join('\n')
    }

    const downloadCSV = (content, filename) => {
      const blob = new Blob(['\uFEFF' + content], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', filename)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

    onMounted(() => {
      refreshResults()
    })

    return {
      loading,
      activity,
      results,
      seats,
      filterType,
      summary,
      filteredResults,
      finalSelections,
      getSeatLabel,
      getAssignmentType,
      getStatusType,
      getStatusText,
      getDescription,
      getDescriptionClass,
      formatDate,
      refreshResults,
      exportResults
    }
  }
}
</script>

<style scoped>
.activity-result {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.loading-container {
  min-height: 400px;
}

.summary-card,
.results-table,
.seat-map-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  display: flex;
  gap: 10px;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.seat-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.legend-color.original {
  background-color: #67c23a;
}

.legend-color.reassigned {
  background-color: #e6a23c;
}

.legend-color.empty {
  background-color: #f0f0f0;
}
</style>
