package com.seatlottery.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化代碼，如果需要的話
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        //要更改！！！！！！！！！
        // 允許跨域請求的來源
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");

        // 允許的請求方法
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // 允許的請求頭
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");

        // 預檢請求的有效期，單位秒
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // 是否允許發送Cookie
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // 處理 OPTIONS 預檢請求
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 繼續過濾鏈
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清理代碼，如果需要的話
    }
}