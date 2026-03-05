<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-message">頁面未找到</h2>
      <p class="error-description">
        抱歉，這裡沒有東西啦。
      </p>
      <div class="action-buttons">
        <button @click="goBack" class="btn btn-secondary">
          返回上一頁
        </button>
        <router-link to="/admin" class="btn btn-primary">
          回到首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFound',
  methods: {
    goBack() {
      if (window.history.length > 1) {
        this.$router.go(-1)
      } else {
        this.$router.push('/admin')
      }
    }
  }
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.not-found-content {
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.error-code {
  font-size: 6rem;
  font-weight: bold;
  color: #e74c3c;
  margin: 0;
  line-height: 1;
}

.error-message {
  font-size: 2rem;
  color: #2c3e50;
  margin: 20px 0 10px;
}

.error-description {
  color: #7f8c8d;
  font-size: 1.1rem;
  margin-bottom: 30px;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.3s ease;
  display: inline-block;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-secondary {
  background-color: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background-color: #7f8c8d;
}

@media (max-width: 480px) {
  .error-code {
    font-size: 4rem;
  }

  .error-message {
    font-size: 1.5rem;
  }

  .action-buttons {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
