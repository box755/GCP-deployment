<template>
  <div class="join-activity">
    <div class="container">
      <el-card class="join-card">
        <template #header>
          <div class="card-header">
            <h2>加入活動</h2>
          </div>
        </template>

        <el-loading v-loading="loading" class="loading-container">
          <div v-if="activity" class="activity-info">
            <h3>{{ activity.name }}</h3>
            <p class="description">{{ activity.description }}</p>

            <div class="time-info">
              <div class="time-item">
                <strong>選位開始時間：</strong>
                <span>{{ formatDate(activity.selectionStartTime) }}</span>
              </div>
              <div class="time-item">
                <strong>選位結束時間：</strong>
                <span>{{ formatDate(activity.selectionEndTime) }}</span>
              </div>
            </div>

            <div class="status-info">
              <el-alert
                  v-if="!isSelectionTime"
                  :title="getStatusMessage()"
                  :type="getStatusType()"
                  show-icon
                  :closable="false"
              />
            </div>

            <el-form
                v-if="!sessionUser && isSelectionTime"
                ref="formRef"
                :model="form"
                :rules="rules"
                :label-width="labelWidth"
                class="join-form"
            >
              <el-form-item label="姓名" prop="name">
                <el-input
                    v-model="form.name"
                    placeholder="請輸入您的姓名"
                    maxlength="20"
                    show-word-limit
                />
              </el-form-item>

              <el-form-item label="聯繫方式" prop="contact">
                <el-input
                    v-model="form.contact"
                    placeholder="請輸入聯繫方式（可選）"
                    maxlength="50"
                />
              </el-form-item>

              <div class="form-actions">
                <el-button
                    type="primary"
                    @click="joinActivity"
                    :loading="userLoading"
                    :size="buttonSize"
                    class="join-button"
                >
                  加入活動
                </el-button>
              </div>
            </el-form>

            <div v-else-if="sessionUser" class="joined-info">
              <el-result
                  icon="success"
                  title="已成功加入活動"
                  :sub-title="`歡迎，${sessionUser.name}！`"
              >
                <template #extra>
                  <el-button
                      type="primary"
                      @click="goToSeatSelection"
                      :disabled="!isSelectionTime"
                      :size="buttonSize"
                      class="action-button"
                  >
                    前往選座
                  </el-button>
                </template>
              </el-result>
            </div>
          </div>

          <div v-else-if="error" class="error-state">
            <el-result
                icon="error"
                title="活動不存在"
                sub-title="請檢查活動網址是否正確"
            >
              <template #extra>
                <el-button @click="retryLoad" :size="buttonSize">重新載入</el-button>
              </template>
            </el-result>
          </div>
        </el-loading>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

export default {
  name: 'UserJoin',
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
    const formRef = ref()

    // 從 sessionStorage 獲取會話用戶，用於記住當前參與者
    const sessionUser = ref(JSON.parse(sessionStorage.getItem(`activity_${props.activityId}_user`)))

    const form = reactive({
      name: '',
      contact: ''
    })

    const rules = {
      name: [
        { required: true, message: '請輸入姓名', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名長度應在 2 到 20 個字符', trigger: 'blur' }
      ]
    }

    const activity = computed(() => activityStore.currentActivity)
    const loading = computed(() => activityStore.loading)
    const error = computed(() => activityStore.error)
    const userLoading = computed(() => userStore.loading)

    // 響應式計算屬性
    const labelWidth = computed(() => {
      if (window.innerWidth < 480) return '80px'
      if (window.innerWidth < 768) return '90px'
      return '100px'
    })

    const buttonSize = computed(() => {
      if (window.innerWidth < 480) return 'default'
      if (window.innerWidth < 768) return 'large'
      return 'large'
    })

    const isSelectionTime = computed(() => {
      if (!activity.value) return false
      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)
      return now >= startTime && now <= endTime
    })

    const formatDate = (dateString) => {
      if (!dateString) return '未設定'
      const date = new Date(dateString)
      return date.toLocaleString('zh-TW')
    }

    const getStatusMessage = () => {
      if (!activity.value) return ''
      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)

      if (now < startTime) {
        return '選位尚未開始，請耐心等待'
      } else if (now > endTime) {
        return '選位時間已結束'
      }
      return ''
    }

    const getStatusType = () => {
      if (!activity.value) return 'info'
      const now = new Date()
      const startTime = new Date(activity.value.selectionStartTime)
      const endTime = new Date(activity.value.selectionEndTime)

      if (now < startTime) {
        return 'warning'
      } else if (now > endTime) {
        return 'error'
      }
      return 'success'
    }

    const joinActivity = async () => {
      try {
        await formRef.value.validate()

        const userData = {
          name: form.name,
          contact: form.contact,
          joinTime: new Date().toISOString()
        }

        // 將用戶資料存入 API
        const userResponse = await userStore.joinActivity(props.activityId, userData)

        // 在會話中保存用戶信息
        sessionStorage.setItem(`activity_${props.activityId}_user`, JSON.stringify(userResponse))
        sessionUser.value = userResponse

        ElMessage.success('成功加入活動')
      } catch (error) {
        if (error.errors) {
          ElMessage.error('請檢查表單內容')
        } else {
          ElMessage.error('加入失敗，請稍後重試')
        }
      }
    }

    const goToSeatSelection = () => {
      router.push(`/activity/${props.activityId}/select`)
    }

    const retryLoad = () => {
      activityStore.fetchActivity(props.activityId)
    }

    onMounted(() => {
      activityStore.fetchActivity(props.activityId)

      // 如果已有會話用戶，嘗試獲取其選座信息
      if (sessionUser.value) {
        userStore.getUserSelection(props.activityId, sessionUser.value.id)
            .catch(() => {
              // 如果獲取失敗，可能是會話已過期，清除會話用戶
              sessionStorage.removeItem(`activity_${props.activityId}_user`)
              sessionUser.value = null
            })
      }
    })

    return {
      formRef,
      form,
      rules,
      activity,
      loading,
      error,
      userLoading,
      sessionUser,
      labelWidth,
      buttonSize,
      isSelectionTime,
      formatDate,
      getStatusMessage,
      getStatusType,
      joinActivity,
      goToSeatSelection,
      retryLoad
    }
  }
}
</script>

<style scoped>
.join-activity {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.container {
  width: 100%;
  max-width: 600px;
}

.join-card {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  overflow: hidden;
}

.card-header h2 {
  margin: 0;
  text-align: center;
  color: #303133;
  font-size: 24px;
}

.loading-container {
  min-height: 200px;
}

.activity-info h3 {
  text-align: center;
  color: #303133;
  margin-bottom: 15px;
  font-size: 20px;
  line-height: 1.4;
}

.description {
  text-align: center;
  color: #606266;
  margin-bottom: 30px;
  line-height: 1.6;
  font-size: 16px;
}

.time-info {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.time-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
}

.time-item:last-child {
  margin-bottom: 0;
}

.time-item strong {
  color: #303133;
  font-size: 14px;
  white-space: nowrap;
}

.time-item span {
  text-align: right;
  color: #606266;
  font-size: 14px;
  word-break: break-all;
}

.status-info {
  margin-bottom: 30px;
}

.join-form {
  margin-top: 30px;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
}

.join-button {
  width: 100%;
  max-width: 300px;
  padding: 12px 24px;
}

.action-button {
  padding: 12px 24px;
  min-width: 140px;
}

.joined-info {
  text-align: center;
  margin-top: 30px;
}

.error-state {
  text-align: center;
  margin-top: 30px;
}

/* 平板樣式 */
@media (max-width: 768px) {
  .join-activity {
    padding: 15px;
  }

  .container {
    max-width: 100%;
  }

  .card-header h2 {
    font-size: 20px;
  }

  .activity-info h3 {
    font-size: 18px;
  }

  .description {
    font-size: 15px;
  }

  .time-info {
    padding: 15px;
  }

  .time-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .time-item span {
    text-align: left;
  }

  .join-button {
    max-width: 100%;
    padding: 14px 28px;
  }
}

/* 手機樣式 */
@media (max-width: 480px) {
  .join-activity {
    padding: 10px;
    align-items: flex-start;
    padding-top: 40px;
  }

  .join-card {
    border-radius: 8px;
  }

  .card-header h2 {
    font-size: 18px;
  }

  .activity-info h3 {
    font-size: 16px;
    margin-bottom: 12px;
  }

  .description {
    font-size: 14px;
    margin-bottom: 20px;
  }

  .time-info {
    padding: 12px;
    margin-bottom: 15px;
  }

  .time-item strong,
  .time-item span {
    font-size: 13px;
  }

  .status-info {
    margin-bottom: 20px;
  }

  .join-form {
    margin-top: 20px;
  }

  .form-actions {
    margin-top: 20px;
  }

  .join-button {
    padding: 12px 20px;
    font-size: 15px;
  }

  .action-button {
    padding: 10px 20px;
    font-size: 15px;
    min-width: 120px;
  }

  .joined-info {
    margin-top: 20px;
  }

  .error-state {
    margin-top: 20px;
  }
}

/* 小手機樣式 */
@media (max-width: 360px) {
  .join-activity {
    padding: 8px;
    padding-top: 20px;
  }

  .card-header h2 {
    font-size: 16px;
  }

  .activity-info h3 {
    font-size: 15px;
  }

  .description {
    font-size: 13px;
  }

  .time-info {
    padding: 10px;
  }

  .time-item strong,
  .time-item span {
    font-size: 12px;
  }

  .join-button,
  .action-button {
    font-size: 14px;
    padding: 10px 16px;
  }
}
</style>
