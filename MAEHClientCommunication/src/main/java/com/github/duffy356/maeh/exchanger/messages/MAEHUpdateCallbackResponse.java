package com.github.duffy356.maeh.exchanger.messages;

import com.github.duffy356.maeh.exchanger.messages.part.CallbackIntend;

import java.util.List;

/**
 * Created by doba on 13.04.2014.
 */
public class MAEHUpdateCallbackResponse extends AbstractMAEHMessage {
    private List<CallbackIntend> newCallbacks;
    private List<CallbackIntend> removedCallbacks;

    public MAEHUpdateCallbackResponse() {
        super(MessageType.INT_RES);
    }

    public List<CallbackIntend> getNewCallbacks() {
        return newCallbacks;
    }

    public void setNewCallbacks(List<CallbackIntend> newCallbacks) {
        this.newCallbacks = newCallbacks;
    }

    public List<CallbackIntend> getRemovedCallbacks() {
        return removedCallbacks;
    }

    public void setRemovedCallbacks(List<CallbackIntend> removedCallbacks) {
        this.removedCallbacks = removedCallbacks;
    }
}
