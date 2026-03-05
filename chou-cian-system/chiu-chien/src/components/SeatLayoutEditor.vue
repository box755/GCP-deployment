<template>
  <div class="seat-layout-editor">
    <div class="toolbar">
      <div class="mode-selector">
        <el-button-group>
          <el-button
              :type="editMode === 'add' ? 'primary' : 'default'"
              @click="setEditMode('add')"
              size="small"
          >
            <el-icon><Plus /></el-icon> 添加座位
          </el-button>
          <el-button
              :type="editMode === 'remove' ? 'primary' : 'default'"
              @click="setEditMode('remove')"
              size="small"
          >
            <el-icon><Delete /></el-icon> 移除座位
          </el-button>
        </el-button-group>
      </div>

      <div class="seat-info">
        <el-tag type="success" size="large" effect="plain">
          可用座位數：{{ availableSeats }} 個
        </el-tag>
      </div>
    </div>

    <!-- 分頁控制 - 上方 -->
    <div class="pagination-controls" v-if="totalPages > 1">
      <el-button
          type="primary"
          plain
          @click="handlePageChange(currentPage - 1)"
          :disabled="currentPage <= 1"
          size="small"
      >
        <el-icon><ArrowUp /></el-icon>
        上一頁
      </el-button>
    </div>

    <!-- 固定高度的座位預覽容器 -->
    <div class="seat-preview-wrapper">
      <!-- 左側分頁控制 -->
      <div class="side-control left-control" v-if="totalColumns > 1">
        <el-button
            circle
            @click="handleColumnChange(currentColumn - 1)"
            :disabled="currentColumn <= 1"
            size="small"
        >
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
      </div>

      <div class="seat-preview-container">
        <div class="page-indicator">
          {{ currentPage }}/{{ totalPages }} 頁
          {{ currentColumn }}/{{ totalColumns }} 列
        </div>

        <div ref="gridContainerRef" class="seat-grid-container">
          <div class="seat-grid" :style="gridStyle">
            <div
                v-for="seat in visibleSeats"
                :key="seat.id"
                :class="getSeatClass(seat)"
                @click="handleSeatClick(seat)"
            >
              <span v-if="seat.available">{{ seat.label }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右側分頁控制 -->
      <div class="side-control right-control" v-if="totalColumns > 1">
        <el-button
            circle
            @click="handleColumnChange(currentColumn + 1)"
            :disabled="currentColumn >= totalColumns"
            size="small"
        >
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 分頁控制 - 下方 -->
    <div class="pagination-controls" v-if="totalPages > 1">
      <el-button
          type="primary"
          plain
          @click="handlePageChange(currentPage + 1)"
          :disabled="currentPage >= totalPages"
          size="small"
      >
        <el-icon><ArrowDown /></el-icon>
        下一頁
      </el-button>
    </div>

    <div class="legend">
      <div class="legend-item">
        <div class="seat available"></div>
        <span>可用座位</span>
      </div>
      <div class="legend-item">
        <div class="seat unavailable"></div>
        <span>不可用</span>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { Plus, Delete, ArrowUp, ArrowDown, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

export default {
  name: 'SeatLayoutEditor',
  components: {
    Plus,
    Delete,
    ArrowUp,
    ArrowDown,
    ArrowLeft,
    ArrowRight
  },
  props: {
    modelValue: {
      type: Array,
      default: () => []
    },
    rows: {
      type: Number,
      default: 5
    },
    cols: {
      type: Number,
      default: 8
    }
  },
  emits: ['update:modelValue', 'update'],
  setup(props, { emit }) {
    const editMode = ref('add')
    const seatLayout = ref([...props.modelValue])
    const currentPage = ref(1)  // 控制上下頁
    const currentColumn = ref(1)  // 控制左右頁
    const gridContainerRef = ref(null)

    // 固定每頁顯示的行數和列數 - 增加顯示數量
    const rowsPerPage = ref(8)  // 每頁顯示8行
    const colsPerPage = ref(8)  // 每頁顯示8列

    // 計算總行數和總列數
    const totalPages = computed(() => Math.ceil(props.rows / rowsPerPage.value))
    const totalColumns = computed(() => Math.ceil(props.cols / colsPerPage.value))

    // 當前頁的座位，計算當前視圖需要顯示的座位
    const visibleSeats = computed(() => {
      const result = []

      // 計算當前頁面應該顯示哪些行
      const startRow = (currentPage.value - 1) * rowsPerPage.value
      const endRow = Math.min(startRow + rowsPerPage.value, props.rows)

      // 計算當前列應該顯示哪些列
      const startCol = (currentColumn.value - 1) * colsPerPage.value
      const endCol = Math.min(startCol + colsPerPage.value, props.cols)

      // 篩選出當前應該顯示的座位
      for (const seat of seatLayout.value) {
        if (seat.row >= startRow && seat.row < endRow &&
            seat.col >= startCol && seat.col < endCol) {
          result.push(seat)
        }
      }

      return result
    })

    // 計算網格樣式，保持固定大小
    const gridStyle = computed(() => {
      // 視圖中的行數和列數
      const visibleRows = Math.min(rowsPerPage.value, props.rows - ((currentPage.value - 1) * rowsPerPage.value))
      const visibleCols = Math.min(colsPerPage.value, props.cols - ((currentColumn.value - 1) * colsPerPage.value))

      // 根據顯示的座位數量動態調整每個座位的大小
      const maxSize = 38  // 最大座位尺寸
      const minSize = 30  // 最小座位尺寸

      // 根據顯示的行列數計算適合的座位尺寸
      const seatSize = Math.max(
          minSize,
          Math.min(maxSize, Math.floor(280 / Math.max(visibleRows, visibleCols)))
      )

      return {
        display: 'grid',
        gridTemplateColumns: `repeat(${visibleCols}, ${seatSize}px)`,
        gridTemplateRows: `repeat(${visibleRows}, ${seatSize}px)`,
        gap: '4px',
        justifyContent: 'center', // 水平居中
        alignItems: 'center'      // 垂直居中
      }
    })

    const availableSeats = computed(() =>
        seatLayout.value.filter(seat => seat.available).length
    )

    const setEditMode = (mode) => {
      editMode.value = mode
    }

    const getSeatClass = (seat) => ({
      'seat': true,
      'available': seat.available,
      'unavailable': !seat.available
    })

    const handleSeatClick = (seat) => {
      if (editMode.value === 'add') {
        seat.available = true
      } else if (editMode.value === 'remove') {
        seat.available = false
      }
      updateLayout()
    }

    const handlePageChange = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    const handleColumnChange = (column) => {
      if (column >= 1 && column <= totalColumns.value) {
        currentColumn.value = column
      }
    }

    const updateLayout = () => {
      emit('update:modelValue', seatLayout.value)
      emit('update', seatLayout.value)
    }

    // 監聽座位布局數據變化
    watch(() => props.modelValue, (newValue) => {
      seatLayout.value = [...newValue]
    }, { deep: true })

    // 當行數或列數變化時，重置分頁
    watch([() => props.rows, () => props.cols], () => {
      currentPage.value = 1
      currentColumn.value = 1

      // 調整分頁大小以確保良好的顯示效果
      if (props.rows > 15 || props.cols > 15) {
        rowsPerPage.value = 7
        colsPerPage.value = 7
      } else {
        rowsPerPage.value = 8
        colsPerPage.value = 8
      }
    }, { immediate: true })

    return {
      editMode,
      seatLayout,
      gridStyle,
      availableSeats,
      currentPage,
      currentColumn,
      totalPages,
      totalColumns,
      visibleSeats,
      gridContainerRef,
      setEditMode,
      getSeatClass,
      handleSeatClick,
      handlePageChange,
      handleColumnChange
    }
  }
}
</script>

<style scoped>
.seat-layout-editor {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  background-color: #fafafa;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.mode-selector {
  display: flex;
  align-items: center;
}

.seat-info {
  display: flex;
  align-items: center;
  font-weight: 500;
}

/* 分頁控制 */
.pagination-controls {
  display: flex;
  justify-content: center;
  margin: 10px 0;
}

/* 上下左右分頁的容器佈局 */
.seat-preview-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin: 10px 0;
}

.side-control {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
}

/* 固定高度的座位預覽容器 */
.seat-preview-container {
  position: relative;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: white;
  padding: 16px;
  width: 350px; /* 增加寬度 */
  height: 350px; /* 增加高度 */
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  overflow: hidden; /* 防止內容溢出 */
}

.page-indicator {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  z-index: 10;
}

.seat-grid-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seat-grid {
  margin: 0 auto;
}

.seat {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 13px;
  font-weight: bold;
  transition: all 0.2s ease;
}

.seat.available {
  background-color: #67c23a;
  color: white;
  box-shadow: 0 2px 4px rgba(103, 194, 58, 0.3);
}

.seat.available:hover {
  background-color: #85ce61;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(103, 194, 58, 0.5);
}

.seat.unavailable {
  background-color: #f56c6c;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
}

.seat.unavailable:hover {
  background-color: #fb8383;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(245, 108, 108, 0.5);
}

.legend {
  display: flex;
  gap: 24px;
  justify-content: center;
  margin-top: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend .seat {
  width: 24px;
  height: 24px;
  cursor: default;
}

.legend .seat:hover {
  transform: none;
  box-shadow: none;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .seat-preview-container {
    width: 300px;
    height: 300px;
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .seat-preview-container {
    width: 240px;
    height: 240px;
    padding: 8px;
  }

  .seat {
    font-size: 11px;
  }
}
</style>
