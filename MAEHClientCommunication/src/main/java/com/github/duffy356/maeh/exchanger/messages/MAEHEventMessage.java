package com.github.duffy356.maeh.exchanger.messages;

import java.util.Map;

/**
 * Created by doba on 13.04.2014.
 */
public class MAEHEventMessage extends AbstractMAEHMessage {
    private String callback;
    private Map<String, Object> data;
    private Integer senderId;

    public MAEHEventMessage() {
        super(MessageType.EVT_MSG);
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }
}
