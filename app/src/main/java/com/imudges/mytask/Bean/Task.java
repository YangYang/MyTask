package com.imudges.mytask.Bean;


import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.Date;

/**
 * Created by yangyang on 2017/4/26.
 */
@Table(name = "task")
public class Task {

    @Column(name = "id",
            isId = true
    )
    private int id;

    @Column(name="taskName")
    private String taskName;

    @Column(name="userId")
    private String userId;

    @Column(name="summary")
    private String summary;

    @Column(name="addTime")
    private Date addTime;

    /**
     * 1：未完成
     * 0：完成
     * -1:放弃
     * */
    @Column(name="status")
    private int status;

    /**
     * 任务类型
     * 0，1，2，3四个级别
     * */
    @Column(name="type")
    private int type;

    @Column(name="taskWebId")
    private String taskWebId;

    /**
     * 0:已经同步
     * 1：未同步
     * */
    @Column(name="syncStatus")
    private String syncStatus;

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getTaskWebId() {
        return taskWebId;
    }

    public void setTaskWebId(String taskWebId) {
        this.taskWebId = taskWebId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

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
