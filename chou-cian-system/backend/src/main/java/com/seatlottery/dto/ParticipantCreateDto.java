package com.seatlottery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Date;

/**
 * 用於接收參與者創建請求的 DTO 類
 */
public class ParticipantCreateDto {
    private String name;
    private String contact;

    @JsonProperty("joinTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date joinTime;

    // 構造函數
    public ParticipantCreateDto() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }

    @Override
    public String toString() {
        return "ParticipantCreateDto{" +
                "name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                ", joinTime=" + joinTime +
                '}';
    }
}