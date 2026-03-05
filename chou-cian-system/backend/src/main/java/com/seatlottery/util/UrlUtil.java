package com.seatlottery.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {

    public static int extractActivityId(String uri) {
        // 更精確的正則表達式，要求活動 ID 後面跟著 "/" 或者是路徑的結尾
        Pattern pattern = Pattern.compile("/api/activities/(\\d+)(/|$)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從 URI 中提取活動 ID: " + uri);
    }

    /**
     * 從活動詳情路徑中提取活動 ID
     * 僅匹配格式: /api/activities/{activityId} 而不包含其他後續路徑
     */
    public static int extractActivityDetailId(String uri) {
        Pattern pattern = Pattern.compile("/api/activities/(\\d+)$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從活動詳情 URI 中提取活動 ID: " + uri);
    }

    /**
     * 從座位列表路徑中提取活動 ID
     * 匹配格式: /api/activities/{activityId}/seats
     */
    public static int extractActivityIdFromSeats(String uri) {
        // 修改正則表達式以匹配 /api/activities/{activityId}/seats
        Pattern pattern = Pattern.compile("/api/activities/(\\d+)/seats");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從座位列表 URI 中提取活動 ID: " + uri);
    }

    /**
     * 從選座記錄路徑中提取活動 ID
     * 匹配格式: /api/activities/{activityId}/selections
     */
    public static int extractActivityIdFromSelections(String uri) {
        // 修改正則表達式以匹配 /api/activities/{activityId}/selections
        Pattern pattern = Pattern.compile("/api/activities/(\\d+)/selections");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從選座記錄 URI 中提取活動 ID: " + uri);
    }


    /**
     * 從參與者列表路徑中提取活動 ID
     * 僅匹配格式: /api/activities/{activityId}/participants
     */
    public static int extractActivityIdFromParticipants(String uri) {
        Pattern pattern = Pattern.compile("/api/activities/(\\d+)/participants$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從參與者列表 URI 中提取活動 ID: " + uri);
    }

    /**
     * 從抽籤結果路徑中提取活動 ID
     * 僅匹配格式: /api/activities/{activityId}/result
     */
    public static int extractActivityIdFromResult(String uri) {
        Pattern pattern = Pattern.compile("/api/activities/(\\d+)/result$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從抽籤結果 URI 中提取活動 ID: " + uri);
    }

    /**
     * 從抽籤執行路徑中提取活動 ID
     * 僅匹配格式: /api/activities/{activityId}/lottery
     */
    public static int extractActivityIdFromLottery(String uri) {
        Pattern pattern = Pattern.compile("/api/activities/(\\d+)/lottery$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從抽籤執行 URI 中提取活動 ID: " + uri);
    }

    /**
     * 從 URL 中提取參與者 ID
     */
    public static int extractParticipantId(String uri) {
        Pattern pattern = Pattern.compile("/participants/(\\d+)(/|$)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從 URI 中提取參與者 ID: " + uri);
    }

    /**
     * 從參與者選座記錄路徑中提取參與者 ID
     * 僅匹配格式: /api/activities/{activityId}/participants/{participantId}/selection
     */
    public static int extractParticipantIdFromSelection(String uri) {
        Pattern pattern = Pattern.compile("/participants/(\\d+)/selection$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new NumberFormatException("無法從參與者選座記錄 URI 中提取參與者 ID: " + uri);
    }

    /**
     * 從 URL 中提取座位 ID
     */
    public static String extractSeatId(String uri) {
        Pattern pattern = Pattern.compile("/seats/([^/]+)(/|$)");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("無法從 URI 中提取座位 ID: " + uri);
    }

    /**
     * 從座位選擇路徑中提取座位 ID
     * 僅匹配格式: /api/activities/{activityId}/seats/{seatId}/select
     */
    public static String extractSeatIdFromSelect(String uri) {
        Pattern pattern = Pattern.compile("/seats/([^/]+)/select$");
        Matcher matcher = pattern.matcher(uri);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("無法從座位選擇 URI 中提取座位 ID: " + uri);
    }

    /**
     * 提取 URL 中的最後一個路徑段
     */
    public static String extractLastPathSegment(String uri) {
        String[] segments = uri.split("/");
        if (segments.length > 0) {
            return segments[segments.length - 1];
        }
        return "";
    }
}