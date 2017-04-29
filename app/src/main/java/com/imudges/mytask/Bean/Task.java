package com.imudges.mytask.Bean;


import java.util.Date;

/**
 * Created by yangyang on 2017/4/26.
 */
public class Task {

    private int id;


    private String userId;


    private String summary;


    private Date addTime;
    /**
     * 1：未完成
     * 0：完成
     * */
    private int status;


    /**
     * 任务类型
     * 0，1，2，3四个级别
     * */
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}
