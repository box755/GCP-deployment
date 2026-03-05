package com.seatlottery.listener;

import com.seatlottery.scheduler.ActivityStatusScheduler;
import java.util.logging.Logger;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(AppContextListener.class.getName());
    private ActivityStatusScheduler statusScheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("應用程序啟動，初始化服務...");

        // 啟動活動狀態更新排程器
        statusScheduler = new ActivityStatusScheduler();
        statusScheduler.start();

        // 將排程器存儲在 ServletContext 中，以便稍後停止
        sce.getServletContext().setAttribute("statusScheduler", statusScheduler);

        logger.info("應用程序初始化完成");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("應用程序關閉，清理資源...");

        // 停止排程器
        ActivityStatusScheduler scheduler =
                (ActivityStatusScheduler) sce.getServletContext().getAttribute("statusScheduler");
        if (scheduler != null) {
            scheduler.stop();
        }

        logger.info("應用程序關閉完成");
    }
}