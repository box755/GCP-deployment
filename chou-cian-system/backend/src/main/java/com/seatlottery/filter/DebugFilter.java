package com.seatlottery.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter("/*")
public class DebugFilter implements Filter {
    private static final Logger logger = Logger.getLogger(DebugFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String uri = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            System.out.println("查詢參數: " + httpRequest.getQueryString());

            logger.info("=== 路由調試 ===");
            logger.info("方法: " + method);
            logger.info("URI: " + uri);
            logger.info("查詢參數: " + httpRequest.getQueryString());
            logger.info("===============");
        }

        chain.doFilter(request, response);
    }
}