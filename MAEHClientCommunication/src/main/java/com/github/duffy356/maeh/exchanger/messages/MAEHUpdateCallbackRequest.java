package com.github.duffy356.maeh.exchanger.messages;

import java.util.List;

/**
 * Created by doba on 13.04.2014.
 */
public class MAEHUpdateCallbackRequest extends AbstractMAEHMessage {
    private Integer maehWebsocketId;
    private List<String> newCallbacks;
    private List<String> removedCallbacks;

    public MAEHUpdateCallbackRequest() {
        super(MessageType.INT_REQ);
    }

    public Integer getMaehWebsocketId() {
        return maehWebsocketId;
    }

    public void setMaehWebsocketId(Integer maehWebsocketId) {
        this.maehWebsocketId = maehWebsocketId;
    }

    public List<String> getNewCallbacks() {
        return newCallbacks;
    }

    public void setNewCallbacks(List<String> newCallbacks) {
        this.newCallbacks = newCallbacks;
    }

    public List<String> getRemovedCallbacks() {
        return removedCallbacks;
    }

    public void setRemovedCallbacks(List<String> removedCallbacks) {
        this.removedCallbacks = removedCallbacks;
    }
}
