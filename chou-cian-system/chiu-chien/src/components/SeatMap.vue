<template>
  <div class="seat-map">
    <div class="map-container">
      <div class="classroom-title">教室座位圖</div>

      <div class="seat-grid" :style="gridStyle">
        <div
            v-for="seat in seats"
            :key="seat.id"
            :class="getSeatClass(seat)"
            @click="handleSeatClick(seat)"
        >
          <div class="seat-label">{{ seat.label }}</div>
          <!-- 修正：使用 seat.seatId 而不是 seat.id -->
          <div v-if="showNames && getSeatUsers(seat.seatId).length > 0" class="seat-users">
            <div
                v-for="user in getSeatUsers(seat.seatId)"
                :key="user.id"
                class="user-name"
            >
              {{ user.name }}
            </div>
          </div>
        </div>
      </div>

      <div class="legend">
        <div class="legend-item">
          <div class="seat-sample available"></div>
          <span>可選座位</span>
        </div>
        <div class="legend-item">
          <div class="seat-sample selected"></div>
          <span>已選座位</span>
        </div>
        <div class="legend-item">
          <div class="seat-sample conflict"></div>
          <span>衝突座位</span>
        </div>
        <div class="legend-item">
          <div class="seat-sample unavailable"></div>
          <span>不可選</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'SeatMap',
  props: {
    seats: {
      type: Array,
      default: () => []
    },
    selections: {
      type: Array,
      default: () => []
    },
    showNames: {
      type: Boolean,
      default: false
    },
    interactive: {
      type: Boolean,
      default: true
    },
    selectedSeat: {
      type: String,  // 這個應該是 seatId 格式（如 "0-0"）
      default: null
    }
  },
  emits: ['seat-click'],
  setup(props, { emit }) {
    const gridStyle = computed(() => {
      if (props.seats.length === 0) return {}

      const maxCol = Math.max(...props.seats.map(seat => seat.col)) + 1
      return {
        gridTemplateColumns: `repeat(${maxCol}, 1fr)`
      }
    })

    // 修正：使用 seatId 來查找選座記錄
    const getSeatUsers = (seatId) => {
      return props.selections.filter(selection => selection.seatId === seatId)
    }

    const getSeatClass = (seat) => {
      // 修正：使用 seat.seatId 而不是 seat.id
      const users = getSeatUsers(seat.seatId)
      const isSelected = props.selectedSeat === seat.seatId  // 修正：比較 seatId

      return {
        'seat': true,
        'available': seat.available && users.length === 0,
        'selected': users.length === 1,
        'conflict': users.length > 1,
        'unavailable': !seat.available,
        'current-selection': isSelected,
        'interactive': props.interactive
      }
    }

    const handleSeatClick = (seat) => {
      if (props.interactive) {
        console.log('SeatMap: 點擊座位', {
          id: seat.id,
          seatId: seat.seatId,
          label: seat.label
        })
        emit('seat-click', seat)
      }
    }

    return {
      gridStyle,
      getSeatUsers,
      getSeatClass,
      handleSeatClick
    }
  }
}
</script>

<!-- 樣式保持不變 -->
<style scoped>
.seat-map {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.map-container {
  max-width: 100%;
  margin: 0 auto;
}

.classroom-title {
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

.seat-grid {
  display: grid;
  gap: 8px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.seat {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  transition: all 0.3s;
  position: relative;
  border: 2px solid transparent;
}

.seat.interactive {
  cursor: pointer;
}

.seat.available {
  background-color: #e7f5e7;
  color: #67c23a;
  border-color: #67c23a;
}

.seat.available.interactive:hover {
  background-color: #67c23a;
  color: white;
  transform: scale(1.05);
}

.seat.selected {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.seat.conflict {
  background-color: #f56c6c;
  color: white;
  border-color: #f56c6c;
  animation: pulse 1.5s infinite;
}

.seat.unavailable {
  background-color: #f5f7fa;
  color: #c0c4cc;
  border-color: #dcdfe6;
}

.seat.current-selection {
  box-shadow: 0 0 0 3px #409eff;
  z-index: 1;
}

.seat-label {
  font-weight: bold;
  margin-bottom: 2px;
}

.seat-users {
  font-size: 10px;
  line-height: 1.2;
  text-align: center;
}

.user-name {
  margin-bottom: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 50px;
}

.legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.seat-sample {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 2px solid;
}

.seat-sample.available {
  background-color: #e7f5e7;
  border-color: #67c23a;
}

.seat-sample.selected {
  background-color: #409eff;
  border-color: #409eff;
}

.seat-sample.conflict {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.seat-sample.unavailable {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}
</style>
