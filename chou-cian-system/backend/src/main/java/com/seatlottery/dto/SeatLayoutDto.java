package com.seatlottery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用於接收前端座位佈局數據的 DTO 類
 */
public class SeatLayoutDto {
    @JsonProperty("id")
    private String positionId;  // 前端傳來的位置ID，如 "0-0"

    private int row;
    private int col;
    private String label;
    private boolean available;

    // 構造函數
    public SeatLayoutDto() {}

    // Getters and Setters
    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }

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
        return "SeatLayoutDto{" +
                "positionId='" + positionId + '\'' +
                ", row=" + row +
                ", col=" + col +
                ", label='" + label + '\'' +
                ", available=" + available +
                '}';
    }
}