package com.seatlottery.model;

import java.util.Date;
import java.util.List;

public class LotteryResult {
    private int id;
    private int activityId;
    private int participantId;
    private String seatId;
    private boolean success;
    private int conflictCount;
    private Date lotteryTime;

    // 額外欄位 (不存於資料庫)
    private String userName;
    private String seatLabel;
    private List<String> conflictUsers;

    private String originalSeatId;      // 原始選擇的座位
    private String assignedSeatId;      // 最終分配的座位
    private String status;              // assigned, no_seat
    private boolean conflictResolved;   // 是否解決了衝突
    private boolean reassigned;         // 是否重新分配了座位

    // 建構函式
    public LotteryResult() {}

    public LotteryResult(int id, int activityId, int participantId, String seatId,
                         boolean success, int conflictCount, Date lotteryTime) {
        this.id = id;
        this.activityId = activityId;
        this.participantId = participantId;
        this.seatId = seatId;
        this.success = success;
        this.conflictCount = conflictCount;
        this.lotteryTime = lotteryTime;
    }

    // 所有的 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(int conflictCount) {
        this.conflictCount = conflictCount;
    }

    public Date getLotteryTime() {
        return lotteryTime;
    }

    public void setLotteryTime(Date lotteryTime) {
        this.lotteryTime = lotteryTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public List<String> getConflictUsers() {
        return conflictUsers;
    }

    public void setConflictUsers(List<String> conflictUsers) {
        this.conflictUsers = conflictUsers;
    }

    // 新增的缺少方法
    public String getOriginalSeatId() {
        return originalSeatId;
    }

    public void setOriginalSeatId(String originalSeatId) {
        this.originalSeatId = originalSeatId;
    }

    public String getAssignedSeatId() {
        return assignedSeatId;
    }

    public void setAssignedSeatId(String assignedSeatId) {
        this.assignedSeatId = assignedSeatId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isConflictResolved() {
        return conflictResolved;
    }

    public void setConflictResolved(boolean conflictResolved) {
        this.conflictResolved = conflictResolved;
    }

    public boolean isReassigned() {
        return reassigned;
    }

    public void setReassigned(boolean reassigned) {
        this.reassigned = reassigned;
    }

    // 便利方法，用於新的抽籤邏輯
    public void setParticipantName(String participantName) {
        this.userName = participantName;
    }

    public String getParticipantName() {
        return this.userName;
    }
}