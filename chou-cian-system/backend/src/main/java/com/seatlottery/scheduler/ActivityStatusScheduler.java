package com.seatlottery.scheduler;

import com.seatlottery.service.ActivityService;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * 活動狀態定期更新排程器
 */
public class ActivityStatusScheduler {
    private static final Logger logger = Logger.getLogger(ActivityStatusScheduler.class.getName());
    private Timer timer;
    private ActivityService activityService;

    public ActivityStatusScheduler() {
        this.activityService = new ActivityService();
    }

    /**
     * 啟動排程器，每分鐘更新一次活動狀態
     */
    public void start() {
        timer = new Timer("ActivityStatusUpdater", true);

        // 立即執行一次，然後每分鐘執行一次
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    logger.info("定期更新活動狀態開始");
                    activityService.updateAllActivityStatuses();
                    logger.info("定期更新活動狀態完成");
                } catch (SQLException e) {
                    logger.severe("定期更新活動狀態失敗: " + e.getMessage());
                }
            }
        }, 0, 60000); // 0ms 延遲，60000ms (1分鐘) 間隔

        logger.info("活動狀態排程器已啟動");
    }

    /**
     * 停止排程器
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            logger.info("活動狀態排程器已停止");
        }
    }
}