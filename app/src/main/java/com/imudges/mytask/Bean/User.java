package com.imudges.mytask.Bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.Date;

/**
 * Created by yangyang on 2017/4/28.
 */
@Table(name="user")
public class User {

    @Column(name = "id",
            isId = true,
            autoGen = true
    )
    private int id;

    @Column(name="username",
            property = "NOT NULL"
    )
    private String username;

    @Column(name="password",
            property = "NOT NULL"
    )
    private String password;

    @Column(name="salt")
    private String salt;

    @Column(name="registerTime")
    private Date registerTime;

    @Column(name="ak")
    private String ak;

    /**
     * 0 : user
     * 1 : admin
     * */

    @Column(name="privilege")
    private int privilege;

    //时间戳
    @Column(name="ts")
    private long ts;

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public int getPrivilege() {
        return privilege;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }
}
