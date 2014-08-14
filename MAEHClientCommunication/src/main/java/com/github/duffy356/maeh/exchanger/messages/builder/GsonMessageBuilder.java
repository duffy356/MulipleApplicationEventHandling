package com.github.duffy356.maeh.exchanger.messages.builder;

import com.github.duffy356.maeh.exchanger.messages.MessageType;
import com.github.duffy356.maeh.exchanger.messages.*;
import com.github.duffy356.maeh.exchanger.messages.part.CallbackIntend;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by doba on 03.05.2014.
 */
public class GsonMessageBuilder implements MessageBuilder {
    private final Gson gsonInstance;
    public GsonMessageBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MessageType.class, new MessageTypeDeserializer());
        gsonBuilder.registerTypeAdapter(CallbackIntend.class, new CallbackIntendDeserializer());
        this.gsonInstance = gsonBuilder.create();
    }

    @Override
    public String toJSON(AbstractMAEHMessage message) {
        String json = null;

        switch (message.getMessagetype()) {

            default:
                json = gsonInstance.toJson(message);
                break;
        }
        return json;
    }

    @Override
    public AbstractMAEHMessage toMessage(String json) {
        AbstractMAEHMessage message = null;

        message = gsonInstance.fromJson(json, MAEHMessageType.class);

        switch (message.getMessagetype()) {
            case REG_RES:
                return gsonInstance.fromJson(json, MAEHRegistrationResponse.class);
            case INT_RES:
                return gsonInstance.fromJson(json, MAEHUpdateCallbackResponse.class);
            case EVT_MSG:
                return gsonInstance.fromJson(json, MAEHEventMessage.class);
            case EVT_RES:
                return gsonInstance.fromJson(json, MAEHEventResponse.class);
        }

        return message;
    }
}
