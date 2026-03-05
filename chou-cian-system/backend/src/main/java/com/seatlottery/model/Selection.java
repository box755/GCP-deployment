package com.seatlottery.model;

import java.util.Date;

public class Selection {
    private int id;
    private int activityId;
    private int participantId;
    private String seatId;
    private Date selectionTime;
    private String status; // pending, confirmed, conflicted

    // 額外欄位 (不存於資料庫，用於前端顯示)
    private String userName;

    // 建構函式、Getter、Setter 方法
    public Selection() {}

    public Selection(int id, int activityId, int participantId, String seatId,
                     Date selectionTime, String status) {
        this.id = id;
        this.activityId = activityId;
        this.participantId = participantId;
        this.seatId = seatId;
        this.selectionTime = selectionTime;
        this.status = status;
    }

    // Getters and Setters
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

    public Date getSelectionTime() {
        return selectionTime;
    }

    public void setSelectionTime(Date selectionTime) {
        this.selectionTime = selectionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}