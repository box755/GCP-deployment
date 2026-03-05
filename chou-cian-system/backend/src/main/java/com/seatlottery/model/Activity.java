package com.seatlottery.model;

import java.util.Date;
import java.util.List;

public class Activity {
    private int id;
    private String name;
    private String description;
    private Date selectionStartTime;
    private Date selectionEndTime;
    private int rows;
    private int cols;
    private int totalSeats;
    private int participantCount;
    private String status; // pending, active, completed
    private Date createTime;
    private Date updateTime;
    private List<Seat> seatLayout;

    // 建構函式、Getter、Setter 方法
    public Activity() {}

    public Activity(int id, String name, String description, Date selectionStartTime,
                    Date selectionEndTime, int rows, int cols, int totalSeats,
                    int participantCount, String status, Date createTime, Date updateTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.selectionStartTime = selectionStartTime;
        this.selectionEndTime = selectionEndTime;
        this.rows = rows;
        this.cols = cols;
        this.totalSeats = totalSeats;
        this.participantCount = participantCount;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getSelectionStartTime() {
        return selectionStartTime;
    }

    public void setSelectionStartTime(Date selectionStartTime) {
        this.selectionStartTime = selectionStartTime;
    }

    public Date getSelectionEndTime() {
        return selectionEndTime;
    }

    public void setSelectionEndTime(Date selectionEndTime) {
        this.selectionEndTime = selectionEndTime;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public List<Seat> getSeatLayout() {
        return seatLayout;
    }

    public void setSeatLayout(List<Seat> seatLayout) {
        this.seatLayout = seatLayout;
    }
}