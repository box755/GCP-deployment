<template>
  <el-card class="activity-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="activity-name">{{ activity.name }}</span>
        <el-tag
            :type="getStatusType(activity.status)"
            size="small"
        >
          {{ getStatusText(activity.status) }}
        </el-tag>
      </div>
    </template>

    <div class="activity-info">
      <div class="info-item">
        <el-icon><Calendar /></el-icon>
        <span>選位時間：{{ formatDate(activity.selectionStartTime) }} - {{ formatDate(activity.selectionEndTime) }}</span>
      </div>

      <div class="info-item">
        <el-icon><User /></el-icon>
        <span>參與人數：{{ activity.participantCount || 0 }} 人</span>
      </div>

      <div class="info-item">
        <el-icon><Grid /></el-icon>
        <span>座位數量：{{ activity.totalSeats }} 個</span>
      </div>

      <div class="info-item">
        <el-icon><Link /></el-icon>
        <span class="activity-url">活動網址：
          <el-button
              type="text"
              size="small"
              @click="copyUrl(activity.id)"
          >
            {{ getActivityUrl(activity.id) }}
          </el-button>
        </span>
      </div>
    </div>

    <div class="card-actions">
      <el-button
          type="primary"
          size="small"
          @click="$emit('view', activity)"
      >
        查看詳情
      </el-button>
      <el-button
          type="danger"
          size="small"
          @click="$emit('delete', activity)"
      >
        刪除
      </el-button>
    </div>
  </el-card>
</template>

<script>
import { Calendar, User, Grid, Link } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'ActivityCard',
  components: {
    Calendar,
    User,
    Grid,
    Link
  },
  props: {
    activity: {
      type: Object,
      required: true
    }
  },
  emits: ['view', 'delete'],
  setup() {
    const getStatusType = (status) => {
      const typeMap = {
        'active': 'success',
        'closed': 'warning',     // 新增：選位已結束狀態
        'completed': 'info',
        'pending': 'warning'
      }
      return typeMap[status] || 'info'
    }

    const getStatusText = (status) => {
      const textMap = {
        'active': '進行中',
        'closed': '選位已結束',   // 新增：選位已結束的顯示文字
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

    const getActivityUrl = (activityId) => {
      return `${window.location.origin}/activity/${activityId}`
    }

    const copyUrl = async (activityId) => {
      try {
        const url = getActivityUrl(activityId)
        await navigator.clipboard.writeText(url)
        ElMessage.success('網址已複製到剪貼板')
      } catch (error) {
        ElMessage.error('複製失敗')
      }
    }

    return {
      getStatusType,
      getStatusText,
      formatDate,
      getActivityUrl,
      copyUrl
    }
  }
}
</script>

<style scoped>
.activity-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-name {
  font-weight: bold;
  font-size: 16px;
}

.activity-info {
  margin: 15px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.info-item .el-icon {
  margin-right: 8px;
  color: #909399;
}

.activity-url {
  display: flex;
  align-items: center;
}

.card-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}
</style>
