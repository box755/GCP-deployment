package com.seatlottery.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    public static void writeJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("data", data);

        String json = JsonUtil.toJson(responseMap);

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    public static void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", true);
        responseMap.put("message", message);

        String json = JsonUtil.toJson(responseMap);

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}