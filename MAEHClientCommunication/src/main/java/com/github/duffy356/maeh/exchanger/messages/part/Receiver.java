package com.github.duffy356.maeh.exchanger.messages.part;

import com.github.duffy356.maeh.exchanger.messages.ReceiverType;

/**
 * Created by doba on 16.04.2014.
 */
public class Receiver {
    private ReceiverType receivertype;
    private String callback;
    private String username;
    private String application;
    private Integer id;

    public ReceiverType getReceivertype() {
        return receivertype;
    }

    public void setReceivertype(ReceiverType receivertype) {
        this.receivertype = receivertype;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
