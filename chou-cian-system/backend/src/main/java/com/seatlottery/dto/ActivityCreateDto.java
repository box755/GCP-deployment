package com.seatlottery.dto;

import java.util.Date;
import java.util.List;

/**
 * 用於接收活動創建請求的 DTO 類
 */
public class ActivityCreateDto {
    private String name;
    private String description;
    private Date selectionStartTime;
    private Date selectionEndTime;
    private int rows;
    private int cols;
    private int totalSeats;
    private String status;
    private int participantCount;
    private List<SeatLayoutDto> seatLayout;

    // 構造函數
    public ActivityCreateDto() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getSelectionStartTime() { return selectionStartTime; }
    public void setSelectionStartTime(Date selectionStartTime) { this.selectionStartTime = selectionStartTime; }

    public Date getSelectionEndTime() { return selectionEndTime; }
    public void setSelectionEndTime(Date selectionEndTime) { this.selectionEndTime = selectionEndTime; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public List<SeatLayoutDto> getSeatLayout() { return seatLayout; }
    public void setSeatLayout(List<SeatLayoutDto> seatLayout) { this.seatLayout = seatLayout; }
}