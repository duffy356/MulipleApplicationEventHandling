package com.github.duffy356.maeh.exchanger.messages.builder;

import com.github.duffy356.maeh.exchanger.messages.part.CallbackIntend;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by doba on 03.05.2014.
 */
public class CallbackIntendDeserializer implements JsonDeserializer<CallbackIntend> {
    @Override
    public CallbackIntend deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        CallbackIntend callbackIntend = new CallbackIntend();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        callbackIntend.setId(Integer.valueOf(jsonObject.get("id").getAsString()));
        callbackIntend.setName(jsonObject.get("name").getAsString());
        return callbackIntend;
    }
}
