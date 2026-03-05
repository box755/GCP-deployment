<template>
  <div class="user-layout">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <div class="brand-section">
            <div class="logo">
              <el-icon class="logo-icon"><Grid /></el-icon>
              <span class="brand-name">芳芳ㄉ座位抽籤系統</span>
              <div v-if="currentActivity" class="activity-info">
                <el-icon><Calendar /></el-icon>
                <span>{{ currentActivity.name }}</span>
              </div>
            </div>

          </div>

          <div class="nav-section">
            <el-button
                type="primary"
                plain
                size="large"
                @click="goToAdmin"
                class="admin-btn"
            >
              <el-icon><User /></el-icon>
              管理員入口
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
import { useActivityStore } from '@/stores/activity'
import { Grid, Calendar, House, User } from '@element-plus/icons-vue'

export default {
  name: 'UserLayout',
  components: {
    Grid,
    Calendar,
    House,
    User
  },
  setup() {
    const router = useRouter()
    const activityStore = useActivityStore()

    const currentActivity = computed(() => activityStore.currentActivity)

    const goHome = () => {
      router.push('/')
    }

    const goToAdmin = () => {
      router.push('/login')
    }

    return {
      currentActivity,
      goHome,
      goToAdmin
    }
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
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
  gap: 8px;
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

.activity-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  opacity: 0.9;
  margin-left: 4px;
  margin-top: 4px;

}

.nav-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-btn {
  color: white;
  font-size: 16px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.admin-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 500;
}

.admin-btn:hover {
  background: rgba(255, 255, 255, 0.25);
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

  .activity-info {
    font-size: 14px;
    margin-left: 44px;
  }

  .admin-btn {
    padding: 6px 12px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
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

  .activity-info {
    font-size: 13px;
    margin-left: 38px;
  }

  .nav-btn {
    font-size: 14px;
    padding: 6px 12px;
  }

  .admin-btn .el-icon + span {
    display: none;
  }

  .nav-section {
    gap: 8px;
  }
}
</style>
