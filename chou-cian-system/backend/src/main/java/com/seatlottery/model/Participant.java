package com.seatlottery.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Participant {
    private int id;
    private int activityId;
    private String name;
    private String contact;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date joinTime;

    // 構造函數
    public Participant() {
        this.joinTime = new Date(); // 默認設置為當前時間
    }

    public Participant(int activityId, String name, String contact) {
        this.activityId = activityId;
        this.name = name;
        this.contact = contact;
        this.joinTime = new Date(); // 默認設置為當前時間
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }

    @Override
    public String toString() {
        return "Participant{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                ", joinTime=" + joinTime +
                '}';
    }
}