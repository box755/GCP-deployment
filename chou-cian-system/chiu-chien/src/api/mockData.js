// 模擬活動數據
export const mockActivities = [
    {
        id: '1',
        name: '2025畢業典禮座位抽籤',
        description: '2025年度畢業典禮座位抽籤活動，請所有畢業生於開放時間內進行選位',
        selectionStartTime: '2025-06-13 09:00:00',
        selectionEndTime: '2025-06-16 18:00:00',
        rows: 10,
        cols: 15,
        totalSeats: 120,
        participantCount: 78,
        status: 'active',
        createTime: '2025-05-20 14:30:00',
        seatLayout: generateSeatLayout(10, 15)
    },
    {
        id: '2',
        name: '期末考試座位安排',
        description: '2025學年度第二學期期末考試座位安排，依序選擇您偏好的座位',
        selectionStartTime: '2025-06-01 10:00:00',
        selectionEndTime: '2025-06-05 23:59:59',
        rows: 8,
        cols: 12,
        totalSeats: 80,
        participantCount: 80,
        status: 'completed',
        createTime: '2025-05-15 09:15:00',
        seatLayout: generateSeatLayout(8, 12)
    },
    {
        id: '3',
        name: '系學會年度講座',
        description: '年度專題講座座位抽籤，請於選位時間內完成選位',
        selectionStartTime: '2025-07-01 08:00:00',
        selectionEndTime: '2025-07-05 23:59:59',
        rows: 6,
        cols: 10,
        totalSeats: 50,
        participantCount: 23,
        status: 'pending',
        createTime: '2025-06-10 11:45:00',
        seatLayout: generateSeatLayout(6, 10)
    },
    {
        id: '4',
        name: '教師研習工作坊',
        description: '教師專業發展工作坊座位安排，請提前選擇您的座位',
        selectionStartTime: '2025-06-08 09:00:00',
        selectionEndTime: '2025-06-10 17:00:00',
        rows: 5,
        cols: 8,
        totalSeats: 32,
        participantCount: 29,
        status: 'completed',
        createTime: '2025-05-25 16:20:00',
        seatLayout: generateSeatLayout(5, 8)
    },
    {
        id: '5',
        name: '系友回娘家活動',
        description: '2025年系友回娘家座位安排，請各位系友進行選位',
        selectionStartTime: '2025-07-15 10:00:00',
        selectionEndTime: '2025-07-20 20:00:00',
        rows: 12,
        cols: 18,
        totalSeats: 180,
        participantCount: 42,
        status: 'pending',
        createTime: '2025-06-08 13:10:00',
        seatLayout: generateSeatLayout(12, 18)
    }
]

// 模擬參與者數據
export const mockParticipants = [
    { id: 'p1', activityId: '1', name: '王小明', contact: 'wang@example.com', joinTime: '2025-06-14 09:05:23' },
    { id: 'p2', activityId: '1', name: '李小花', contact: 'li@example.com', joinTime: '2025-06-14 09:15:43' },
    { id: 'p3', activityId: '1', name: '張大山', contact: 'zhang@example.com', joinTime: '2025-06-14 10:02:19' },
    { id: 'p4', activityId: '2', name: '陳小玲', contact: 'chen@example.com', joinTime: '2025-06-01 10:30:45' },
    { id: 'p5', activityId: '2', name: '林大勇', contact: 'lin@example.com', joinTime: '2025-06-01 11:25:37' }
]

// 模擬選位數據
export const mockSelections = [
    { id: 's1', activityId: '1', userId: 'p1', seatId: '0-2', selectionTime: '2025-06-14 09:10:15', status: 'confirmed' },
    { id: 's2', activityId: '1', userId: 'p2', seatId: '1-3', selectionTime: '2025-06-14 09:20:33', status: 'confirmed' },
    { id: 's3', activityId: '1', userId: 'p3', seatId: '2-5', selectionTime: '2025-06-14 10:15:22', status: 'confirmed' },
    { id: 's4', activityId: '2', userId: 'p4', seatId: '3-4', selectionTime: '2025-06-01 10:45:11', status: 'confirmed' },
    { id: 's5', activityId: '2', userId: 'p5', seatId: '4-3', selectionTime: '2025-06-01 11:30:19', status: 'confirmed' }
]

// 模擬抽籤結果數據
export const mockResults = {
    '2': {
        finalAssignments: [
            { userId: 'p4', userName: '陳小玲', seatId: '3-4', seatLabel: '4-E' },
            { userId: 'p5', userName: '林大勇', seatId: '4-3', seatLabel: '5-D' }
        ],
        statistics: {
            totalParticipants: 80,
            totalSeats: 80,
            assignedSeats: 80,
            conflictCount: 15
        },
        detailedResults: [
            {
                userId: 'p4',
                userName: '陳小玲',
                selectedSeat: '4-E',
                finalSeat: '4-E',
                selectionTime: '2025-06-01 10:45:11',
                success: true,
                conflictUsers: []
            },
            {
                userId: 'p5',
                userName: '林大勇',
                selectedSeat: '5-D',
                finalSeat: '5-D',
                selectionTime: '2025-06-01 11:30:19',
                success: true,
                conflictUsers: []
            }
        ],
        lotteryTime: '2025-06-06 10:00:00'
    }
}

// 生成座位佈局
function generateSeatLayout(rows, cols) {
    const layout = []
    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            // 隨機保留一些不可用的座位，模擬真實情境
            const available = Math.random() > 0.1
            layout.push({
                id: `${row}-${col}`,
                row,
                col,
                available,
                label: `${row + 1}-${String.fromCharCode(65 + col)}`
            })
        }
    }
    return layout
}

// 生成隨機 ID
export function generateId() {
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15)
}
