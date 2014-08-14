package com.github.duffy356.maeh.exchanger.messages;

import com.github.duffy356.maeh.exchanger.MAEHAbstractExchanger;
import com.github.duffy356.maeh.exchanger.messages.part.Message;
import com.github.duffy356.maeh.exchanger.messages.part.Receiver;

import java.util.List;
import java.util.Map;

/**
 * Created by doba on 13.04.2014.
 */
public class MAEHEventRequest extends AbstractMAEHMessage {
    private String callback;
    private List<Receiver> receivers;
    private Map<String, Object> data;

    public MAEHEventRequest(Message message) {
        super(MessageType.EVT_REQ);

        this.receivers = message.getReceivers();
        this.data = message.getData();
        this.callback = message.getCallbackName();
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

    public List<Receiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<Receiver> receivers) {
        this.receivers = receivers;
    }

}
