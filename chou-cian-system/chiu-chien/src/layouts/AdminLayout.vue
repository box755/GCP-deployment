<template>
  <div class="admin-layout">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="brand-section">
            <div class="logo">
              <el-icon class="logo-icon"><Grid /></el-icon>
              <span class="brand-name">芳芳ㄉ座位抽籤系統</span>
              <span class="brand-subtitle">管理後台</span>
            </div>
          </div>

          <div class="user-section">
            <div class="user-info">
              <el-icon class="user-icon"><User /></el-icon>
              <span class="username">{{ currentUser }}</span>
            </div>
            <el-button
                class="logout-btn"
                type="danger"
                size="large"
                @click="handleLogout"
            >
              <el-icon><SwitchButton /></el-icon>
              登出
            </el-button>
          </div>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Grid, User, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'AdminLayout',
  components: {
    Grid,
    User,
    SwitchButton
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()

    const currentUser = computed(() => userStore.user?.username || '管理員')

    const handleLogout = async () => {
      try {
        await ElMessageBox.confirm(
            '確定要登出嗎？',
            '確認登出',
            {
              confirmButtonText: '確定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        await userStore.logout()
        ElMessage.success('已成功登出')
        router.push('/login')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('登出失敗')
        }
      }
    }

    return {
      currentUser,
      handleLogout
    }
  }
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
}

.brand-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.brand-name {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand-subtitle {
  font-size: 14px;
  opacity: 0.85;
  margin-top: 4px;
  margin-left: 4px;
  font-weight: 400;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.user-icon {
  font-size: 18px;
}

.username {
  font-size: 15px;
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(236, 65, 65, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 500;
}

.logout-btn:hover {
  background: rgba(236, 65, 65, 1);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.main {
  padding: 0;
  background-color: #f5f7fa;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }

  .brand-name {
    font-size: 20px;
  }

  .brand-subtitle {
    font-size: 12px;
    margin-left: 44px;
  }

  .user-info {
    padding: 6px 12px;
  }

  .username {
    display: none;
  }

  .logout-btn {
    padding: 6px 12px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .brand-section {
    gap: 2px;
  }

  .logo {
    gap: 8px;
  }

  .logo-icon {
    font-size: 24px;
    padding: 6px;
  }

  .brand-name {
    font-size: 18px;
  }

  .brand-subtitle {
    font-size: 11px;
    margin-left: 38px;
  }

  .user-section {
    gap: 12px;
  }

  .logout-btn .el-icon + span {
    display: none;
  }
}
</style>
