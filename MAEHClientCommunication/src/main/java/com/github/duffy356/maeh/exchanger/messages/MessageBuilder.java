package com.github.duffy356.maeh.exchanger.messages;

/**
 * Created by doba on 03.05.2014.
 */
public interface MessageBuilder {
    public String toJSON(AbstractMAEHMessage message);

    public AbstractMAEHMessage toMessage(String json);


}
