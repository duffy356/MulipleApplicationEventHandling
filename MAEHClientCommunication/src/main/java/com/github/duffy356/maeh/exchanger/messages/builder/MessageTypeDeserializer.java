package com.github.duffy356.maeh.exchanger.messages.builder;

import com.github.duffy356.maeh.exchanger.messages.MessageType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by doba on 03.05.2014.
 */
public class MessageTypeDeserializer implements JsonDeserializer<MessageType>
{

    @Override
    public MessageType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.toString().equals(jsonElement.getAsString())) {
                return messageType;
            }
        }

        return null;
    }
}
