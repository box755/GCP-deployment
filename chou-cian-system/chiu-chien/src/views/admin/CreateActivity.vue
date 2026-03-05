<template>
  <div class="create-activity">
    <el-card class="main-card">
      <div class="header">
        <h2>創建新活動</h2>
        <el-button @click="$router.go(-1)" icon="ArrowLeft">返回</el-button>
      </div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          class="activity-form"
      >
        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
          </template>

          <el-form-item label="活動名稱" prop="name">
            <el-input
                v-model="form.name"
                placeholder="請輸入活動名稱"
                maxlength="50"
                show-word-limit
            />
          </el-form-item>

          <el-form-item label="活動描述" prop="description">
            <el-input
                v-model="form.description"
                type="textarea"
                placeholder="請輸入活動描述"
                :rows="3"
                maxlength="200"
                show-word-limit
            />
          </el-form-item>

          <el-form-item label="選位時間" prop="selectionTime">
            <el-date-picker
                v-model="form.selectionTime"
                type="datetimerange"
                range-separator="至"
                start-placeholder="開始時間"
                end-placeholder="結束時間"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-card>

        <el-card class="form-section">
          <template #header>
            <div class="section-header">
              <el-icon><Grid /></el-icon>
              <span>座位配置</span>
            </div>
          </template>

          <div class="grid-config">
            <el-form-item label="行數" prop="rows">
              <el-input-number
                  v-model="form.rows"
                  :min="1"
                  :max="20"
                  @change="updateSeatLayout"
              />
            </el-form-item>

            <el-form-item label="列數" prop="cols">
              <el-input-number
                  v-model="form.cols"
                  :min="1"
                  :max="26"
                  @change="updateSeatLayout"
              />
            </el-form-item>
          </div>

          <el-form-item label="座位設置">
            <div class="seat-setup-hint">
              <el-alert
                  type="info"
                  :closable="false"
                  show-icon
                  title="座位預覽和設置"
              >
                <template #default>
                  點擊座位可以切換其狀態（可用/不可用）。座位過多時會自動分頁顯示。
                </template>
              </el-alert>
            </div>
            <SeatLayoutEditor
                v-model="form.seatLayout"
                :rows="form.rows"
                :cols="form.cols"
                @update="onSeatLayoutUpdate"
            />
          </el-form-item>
        </el-card>

        <div class="form-actions">
          <el-button @click="resetForm" icon="Refresh">重置</el-button>
          <el-button
              type="primary"
              @click="submitForm"
              :loading="loading"
              icon="Check"
          >
            創建活動
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useActivityStore } from '@/stores/activity'
import { ElMessage } from 'element-plus'
import { InfoFilled, Grid, ArrowLeft, Check, Refresh } from '@element-plus/icons-vue'
import SeatLayoutEditor from '@/components/SeatLayoutEditor.vue'

export default {
  name: 'CreateActivity',
  components: {
    SeatLayoutEditor,
    InfoFilled,
    Grid,
    ArrowLeft,
    Check,
    Refresh
  },
  setup() {
    const router = useRouter()
    const activityStore = useActivityStore()
    const formRef = ref()

    const form = reactive({
      name: '',
      description: '',
      selectionTime: '',
      rows: 5,
      cols: 8,
      seatLayout: []
    })

    const rules = {
      name: [
        { required: true, message: '請輸入活動名稱', trigger: 'blur' },
        { min: 2, max: 50, message: '活動名稱長度應在 2 到 50 個字符', trigger: 'blur' }
      ],
      selectionTime: [
        { required: true, message: '請選擇選位時間', trigger: 'change' }
      ]
    }

    const loading = computed(() => activityStore.loading)

    const updateSeatLayout = () => {
      // 確保列數不超過26（A-Z）
      if (form.cols > 26) form.cols = 26

      // 這裡實現網格布局，生成所有座位
      const layout = []
      for (let row = 0; row < form.rows; row++) {
        for (let col = 0; col < form.cols; col++) {
          layout.push({
            id: `${row}-${col}`,
            row,
            col,
            available: true,
            label: `${row + 1}-${String.fromCharCode(65 + col)}`
          })
        }
      }
      form.seatLayout = layout
    }

    const onSeatLayoutUpdate = (layout) => {
      form.seatLayout = layout
    }

    const submitForm = async () => {
      try {
        await formRef.value.validate()

        const availableSeats = form.seatLayout.filter(seat => seat.available).length
        if (availableSeats === 0) {
          ElMessage.warning('請至少設置一個可用座位')
          return
        }

        const activityData = {
          name: form.name,
          description: form.description,
          selectionStartTime: form.selectionTime[0],
          selectionEndTime: form.selectionTime[1],
          rows: form.rows,
          cols: form.cols,
          seatLayout: form.seatLayout,
          totalSeats: availableSeats,
          status: 'pending',
          createTime: new Date().toISOString(),
          participantCount: 0
        }

        await activityStore.createActivity(activityData)
        ElMessage({
          message: '活動創建成功',
          type: 'success',
          duration: 3000
        })
        router.push('/admin')
      } catch (error) {
        if (error.errors) {
          ElMessage.error('請檢查表單內容')
        } else {
          ElMessage.error('創建失敗，請稍後重試')
        }
      }
    }

    const resetForm = () => {
      formRef.value.resetFields()
      form.seatLayout = []
      updateSeatLayout()
    }

    onMounted(() => {
      // 初始化座位佈局
      updateSeatLayout()
    })

    return {
      formRef,
      form,
      rules,
      loading,
      updateSeatLayout,
      onSeatLayoutUpdate,
      submitForm,
      resetForm
    }
  }
}
</script>

<style scoped>
.create-activity {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.main-card {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  margin-bottom: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.activity-form {
  margin-top: 20px;
}

.form-section {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.section-header .el-icon {
  margin-right: 8px;
  color: #409EFF;
  font-size: 20px;
}

.grid-config {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.seat-setup-hint {
  margin-bottom: 16px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-bottom: 16px;
}

.form-actions .el-button {
  padding: 12px 24px;
  font-size: 16px;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .create-activity {
    padding: 12px;
  }

  .grid-config {
    flex-direction: column;
    gap: 8px;
  }

  .form-actions .el-button {
    padding: 10px 20px;
    font-size: 14px;
  }
}
</style>
