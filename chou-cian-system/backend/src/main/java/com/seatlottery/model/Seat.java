package com.seatlottery.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Seat {
    private int id;                // 數據庫自增ID
    private int activityId;
    private String seatId;         // 座位位置標識符，如 "0-0"
    private int row;
    private int col;
    private String label;          // 顯示標籤，如 "1-A"
    private boolean available;

    // 構造函數
    public Seat() {}

    public Seat(int activityId, String seatId, int row, int col, String label, boolean available) {
        this.activityId = activityId;
        this.seatId = seatId;
        this.row = row;
        this.col = col;
        this.label = label;
        this.available = available;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }

    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", seatId='" + seatId + '\'' +
                ", row=" + row +
                ", col=" + col +
                ", label='" + label + '\'' +
                ", available=" + available +
                '}';
    }
}