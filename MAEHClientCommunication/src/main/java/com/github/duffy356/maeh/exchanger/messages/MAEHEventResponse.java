package com.github.duffy356.maeh.exchanger.messages;

import java.util.Map;

/**
 * Created by doba on 13.04.2014.
 */
public class MAEHEventResponse extends AbstractMAEHMessage {
    private Integer receivers;
    private String callback;

    public MAEHEventResponse() {
        super(MessageType.EVT_RES);
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public Integer getReceivers() {
        return receivers;
    }

    public void setReceivers(Integer receivers) {
        this.receivers = receivers;
    }
}
