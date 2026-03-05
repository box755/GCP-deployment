<template>
  <div class="dashboard">
    <div class="header-section">
      <div class="title-area">
        <h1>活動管理中心</h1>
        <p class="subtitle">管理您的座位抽籤活動</p>
      </div>
      <el-button
          type="primary"
          size="large"
          @click="$router.push('/admin/create')"
          :icon="Plus"
      >
        創建新活動
      </el-button>
    </div>

    <!-- 統計卡片 -->
    <div class="stats-section">
      <el-row :gutter="24">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon active">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ activityStats.activeCount }}</div>
                <div class="stats-label">進行中活動</div>
                <div class="stats-detail">
                  進行中: {{ activityStats.activeOnly }} | 已結束: {{ activityStats.closedOnly }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon completed">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ completedActivities.length }}</div>
                <div class="stats-label">已完成活動</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ totalActivities }}</div>
                <div class="stats-label">總活動數</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon participants">
                <el-icon><User /></el-icon>
              </div>
              <div class="stats-info">
                <div class="stats-number">{{ totalParticipants }}</div>
                <div class="stats-label">總參與人數</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-loading v-loading="loading" class="loading-container">
      <div v-if="error" class="error-message">
        <el-alert
            :title="error"
            type="error"
            show-icon
            :closable="false"
        />
      </div>

      <!-- 活動列表 -->
      <div class="activities-section">
        <el-card class="activities-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">活動列表</span>
              <div class="card-actions">
                <el-input
                    v-model="searchKeyword"
                    placeholder="搜尋活動"
                    style="width: 200px"
                    :prefix-icon="Search"
                    clearable
                />
                <el-select
                    v-model="statusFilter"
                    placeholder="篩選狀態"
                    style="width: 150px; margin-left: 10px"
                >
                  <el-option label="全部" value="all" />
                  <el-option label="進行中" value="active" />
                  <el-option label="選位已結束" value="closed" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="待開始" value="pending" />
                </el-select>
              </div>
            </div>
          </template>

          <el-tabs v-model="activeTab" class="activity-tabs">
            <el-tab-pane name="active">
              <template #label>
                <span>進行中 ({{ activityStats.activeCount }})</span>
              </template>
              <div v-if="filteredActiveActivities.length === 0" class="empty-state">
                <el-empty
                    description="暫無進行中的活動"
                    :image-size="120"
                >
                  <el-button
                      type="primary"
                      @click="$router.push('/admin/create')"
                  >
                    立即創建
                  </el-button>
                </el-empty>
              </div>
              <div v-else class="activity-grid">
                <ActivityCard
                    v-for="activity in filteredActiveActivities"
                    :key="activity.id"
                    :activity="activity"
                    @view="viewActivity"
                    @delete="deleteActivity"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane name="completed">
              <template #label>
                <span>已完成 ({{ completedActivities.length }})</span>
              </template>
              <div v-if="filteredCompletedActivities.length === 0" class="empty-state">
                <el-empty
                    description="暫無已完成的活動"
                    :image-size="120"
                />
              </div>
              <div v-else class="activity-grid">
                <ActivityCard
                    v-for="activity in filteredCompletedActivities"
                    :key="activity.id"
                    :activity="activity"
                    @view="viewActivity"
                    @delete="deleteActivity"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane name="all">
              <template #label>
                <span>全部 ({{ totalActivities }})</span>
              </template>
              <div v-if="filteredAllActivities.length === 0" class="empty-state">
                <el-empty
                    description="暫無活動"
                    :image-size="120"
                >
                  <el-button
                      type="primary"
                      @click="$router.push('/admin/create')"
                  >
                    創建第一個活動
                  </el-button>
                </el-empty>
              </div>
              <div v-else class="activity-grid">
                <ActivityCard
                    v-for="activity in filteredAllActivities"
                    :key="activity.id"
                    :activity="activity"
                    @view="viewActivity"
                    @delete="deleteActivity"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </el-loading>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import { Plus, Calendar, Check, Document, User, Search } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import ActivityCard from '@/components/ActivityCard.vue'

export default {
  name: 'Dashboard',
  components: {
    ActivityCard
  },
  setup() {
    const router = useRouter()
    const activityStore = useActivityStore()
    const activeTab = ref('active')
    const searchKeyword = ref('')
    const statusFilter = ref('all')

    const loading = computed(() => activityStore.loading)
    const error = computed(() => activityStore.error)
    const activities = computed(() => activityStore.activities)
    const activeActivities = computed(() => activityStore.activeActivities)
    const completedActivities = computed(() => activityStore.completedActivities)

    // 修正：詳細的活動統計
    const activityStats = computed(() => {
      const stats = activityStore.getActivityCountByStatus
      return {
        activeOnly: stats.active,
        closedOnly: stats.closed,
        activeCount: stats.active + stats.closed, // 進行中包含 active 和 closed
        completed: stats.completed,
        pending: stats.pending,
        total: stats.total
      }
    })

    // 統計數據
    const totalActivities = computed(() => activities.value.length)
    const totalParticipants = computed(() => {
      return activities.value.reduce((total, activity) => {
        return total + (activity.participantCount || 0)
      }, 0)
    })

    // 修正：過濾功能
    const filteredActiveActivities = computed(() => {
      return filterActivities(activeActivities.value)
    })

    const filteredCompletedActivities = computed(() => {
      return filterActivities(completedActivities.value)
    })

    const filteredAllActivities = computed(() => {
      return filterActivities(activities.value)
    })

    const filterActivities = (activityList) => {
      let filtered = activityList

      // 搜尋過濾
      if (searchKeyword.value) {
        filtered = filtered.filter(activity =>
            activity.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
            (activity.description && activity.description.toLowerCase().includes(searchKeyword.value.toLowerCase()))
        )
      }

      // 狀態過濾
      if (statusFilter.value !== 'all') {
        filtered = filtered.filter(activity => activity.status === statusFilter.value)
      }

      return filtered
    }

    const viewActivity = (activity) => {
      router.push(`/admin/activity/${activity.id}`)
    }

    const deleteActivity = async (activity) => {
      try {
        await ElMessageBox.confirm(
            `確定要刪除活動「${activity.name}」嗎？此操作不可撤銷。`,
            '確認刪除',
            {
              confirmButtonText: '確定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        await activityStore.deleteActivity(activity.id)
        ElMessage.success('活動已刪除')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('刪除失敗')
        }
      }
    }

    onMounted(async () => {
      await activityStore.fetchActivities()

      // 調試信息
      console.log('Dashboard 載入完成')
      console.log('活動統計:', activityStats.value)
      console.log('活動列表:', activities.value)
    })

    return {
      activeTab,
      searchKeyword,
      statusFilter,
      loading,
      error,
      activities,
      activeActivities,
      completedActivities,
      totalActivities,
      totalParticipants,
      activityStats,
      filteredActiveActivities,
      filteredCompletedActivities,
      filteredAllActivities,
      viewActivity,
      deleteActivity,
      Plus,
      Calendar,
      Check,
      Document,
      User,
      Search
    }
  }
}
</script>

<style scoped>
/* 原有樣式保持不變，添加新的統計詳情樣式 */
.dashboard {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  border-radius: 12px;
  color: white;
}

.title-area h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.stats-section {
  margin-bottom: 32px;
}

.stats-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.stats-icon.active {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stats-icon.completed {
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.stats-icon.total {
  background: linear-gradient(135deg, #e6a23c, #f7ba2a);
}

.stats-icon.participants {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stats-number {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

/* 新增：統計詳情樣式 */
.stats-detail {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}

.loading-container {
  min-height: 300px;
}

.error-message {
  margin-bottom: 24px;
}

.activities-section {
  margin-bottom: 32px;
}

.activities-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-actions {
  display: flex;
  align-items: center;
}

.activity-tabs {
  margin-top: 0;
}

.activity-tabs :deep(.el-tabs__header) {
  margin: 0 0 24px 0;
}

.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-top: 24px;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
}

/* 響應式設計保持不變 */
@media (max-width: 1200px) {
  .activity-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .header-section {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .stats-section :deep(.el-col) {
    margin-bottom: 16px;
  }

  .card-header {
    flex-direction: column;
    gap: 16px;
  }

  .card-actions {
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .activity-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
