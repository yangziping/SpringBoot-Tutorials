package com.feichaoyu.rabbitmq.model;

import java.io.Serializable;

/**
 * @Author feichaoyu
 * @Date 2019/7/23
 */
public class User implements Serializable {

    private static final long serialVersionUID = 8081849731640304905L;
    private Long id;
    private String userName;
    private String note;

    public User(Long id, String userName, String note) {
        this.id = id;
        this.userName = userName;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
