package com.github.duffy356.maeh.exchanger.messages.part;

import com.github.duffy356.maeh.exchanger.ICallbackName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by doba on 16.04.2014.
 */
public class Message {
    private final static Logger LOGGER = LoggerFactory.getLogger(Message.class);

    private String callbackName;
    private List<Receiver> receivers;
    private Map<String, Object> data;

    public Message(ICallbackName callback) {
        setCallbackName(callback.getCallbackName());
    }

    public String getCallbackName() {
        return callbackName;
    }

    public void setCallbackName(String callbackName) {
        this.callbackName = callbackName;
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


    public void putMessage(String propertyName, String property) {
        if (this.data == null) {
            this.data = new HashMap<String, Object>();
        }

        this.data.put(propertyName, property);
    }

    public void addReceiver(Receiver maehReceiver) {
        if (this.receivers == null) {
            this.receivers = new ArrayList<Receiver>();
        }

        this.receivers.add(maehReceiver);
    }
}
