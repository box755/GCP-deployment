package com.seatlottery.model;

import java.util.Date;

public class Admin {
    private int id;
    private String username;
    private String password;  // 這裡存儲的是密碼哈希，而不是明文密碼
    private String name;
    private String role;
    private Date createTime;
    private Date lastLogin;

    // 建構函式、Getter、Setter 方法
    public Admin() {}

    public Admin(int id, String username, String password, String name, String role,
                 Date createTime, Date lastLogin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.createTime = createTime;
        this.lastLogin = lastLogin;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}