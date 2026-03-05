<template>
  <div class="login-container">
    <div class="login-content">
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="login-header">
            <div class="logo">
              <el-icon class="logo-icon"><Grid /></el-icon>
              <h2>座位抽籤系統</h2>
            </div>
            <p class="subtitle">管理平台登入</p>
          </div>
        </template>

        <el-form
            ref="loginForm"
            :model="form"
            :rules="rules"
            class="login-form"
            @submit.prevent="validateAndLogin"
        >
          <el-form-item prop="username">
            <el-input
                v-model="form.username"
                placeholder="用戶名"
                prefix-icon="User"
                clearable
                size="large"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="form.password"
                type="password"
                placeholder="密碼"
                prefix-icon="Lock"
                show-password
                clearable
                size="large"
            />
          </el-form-item>

          <div class="remember-me">
            <el-checkbox v-model="form.remember">記住我</el-checkbox>
          </div>

          <el-form-item class="login-button-container">
            <el-button
                type="primary"
                native-type="submit"
                :loading="loading"
                size="large"
                class="login-button"
            >
              登入
            </el-button>
          </el-form-item>
        </el-form>

        <div v-if="formError || apiError" class="error-message">
          <el-alert
              :title="formError || apiError"
              type="error"
              :closable="false"
              show-icon
          />
        </div>

        <div class="login-footer">
          <p>© {{ new Date().getFullYear() }} 座位抽籤系統. 保留所有權利.</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Grid, User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  components: {
    Grid,
    User,
    Lock
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const userStore = useUserStore()
    const loginForm = ref(null)
    const formError = ref('') // 表單驗證錯誤
    const apiError = computed(() => userStore.error) // API返回的錯誤

    const form = reactive({
      username: '',
      password: '',
      remember: false
    })

    const rules = {
      username: [
        { required: true, message: '請輸入用戶名', trigger: 'blur' },
        { min: 3, max: 20, message: '長度在 3 到 20 個字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '請輸入密碼', trigger: 'blur' },
        { min: 6, max: 30, message: '長度在 6 到 30 個字符', trigger: 'blur' }
      ]
    }

    const loading = computed(() => userStore.loading)

    const validateAndLogin = async () => {
      // 清除之前的錯誤訊息
      formError.value = ''

      try {
        // 先進行表單驗證
        const valid = await loginForm.value.validate()
        if (valid) {
          // 只有當表單驗證通過時才發送登入請求
          await handleLogin()
        }
      } catch (error) {
        // 驗證失敗，顯示表單錯誤
        formError.value = '請檢查輸入欄位'
        console.error('表單驗證失敗:', error)
      }
    }

    const handleLogin = async () => {
      try {
        await userStore.login(form.username, form.password)

        ElMessage({
          message: '登入成功',
          type: 'success',
          duration: 2000
        })

        // 如果選擇了「記住我」，則儲存用戶名
        if (form.remember) {
          localStorage.setItem('rememberedUsername', form.username)
        } else {
          localStorage.removeItem('rememberedUsername')
        }

        // 若有重定向，則去往該地址，否則進入管理後台
        const redirectUrl = route.query.redirect || '/admin'
        router.push(redirectUrl)
      } catch (error) {
        console.warn(error || '登入失敗，請檢查您的用戶名和密碼')
      }
    }

    // 頁面載入時檢查是否有儲存的用戶名
    const initForm = () => {
      const rememberedUsername = localStorage.getItem('rememberedUsername')
      if (rememberedUsername) {
        form.username = rememberedUsername
        form.remember = true
      }
    }

    // 初始化表單
    initForm()

    return {
      loginForm,
      form,
      rules,
      loading,
      formError,
      apiError,
      validateAndLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.login-content {
  width: 100%;
  max-width: 420px;
}

.login-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.login-card :deep(.el-card__header) {
  padding: 30px;
  background-color: #f9f9f9;
}

.login-header {
  text-align: center;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 12px;
}

.logo-icon {
  font-size: 36px;
  color: #764ba2;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.logo h2 {
  margin: 0;
  font-size: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 700;
}

.subtitle {
  color: #606266;
  font-size: 16px;
  margin: 0;
}

.login-form {
  padding: 30px;
  padding-bottom: 10px;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  height: 50px;
}

.login-form :deep(.el-input__inner) {
  font-size: 16px;
}

.login-form :deep(.el-input__prefix-inner) {
  font-size: 18px;
  color: #909399;
}

.remember-me {
  margin-bottom: 24px;
  color: #606266;
}

.login-button-container {
  margin-bottom: 0;
}

.login-button {
  width: 100%;
  height: 50px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(118, 75, 162, 0.4);
}

.error-message {
  padding: 0 30px;
  margin-bottom: 20px;
}

.login-footer {
  text-align: center;
  padding: 0 30px 30px;
  color: #909399;
  font-size: 13px;
}

.login-footer p {
  margin: 10px 0;
}

/* 響應式設計 */
@media (max-width: 480px) {
  .login-card {
    border-radius: 12px;
  }

  .login-card :deep(.el-card__header) {
    padding: 20px;
  }

  .logo-icon {
    font-size: 28px;
  }

  .logo h2 {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .login-form {
    padding: 20px;
    padding-bottom: 10px;
  }

  .login-form :deep(.el-input__wrapper) {
    height: 46px;
  }

  .login-button {
    height: 46px;
    font-size: 16px;
  }
}
</style>
