package com.imudges.mytask.Bean;

import java.util.Date;

/**
 * Created by yangyang on 2017/4/28.
 */
public class User {

    private int id;

    private String username;


    private String password;


    private String salt;


    private Date registerTime;


    private String ak;

    /**
     * 0 : user
     * 1 : admin
     * */

    private int privilege;

    //时间戳

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
