package com.github.duffy356.maeh.exchanger.messages;

/**
 * Created by doba on 03.05.2014.
 */
public abstract class AbstractMAEHMessage {
    protected MessageType messagetype;

    protected AbstractMAEHMessage(MessageType messagetype) {
        this.messagetype = messagetype;
    }

    public void setMessagetype(MessageType messagetype) {
        this.messagetype = messagetype;
    }

    public MessageType getMessagetype() {
        return messagetype;
    }
}
