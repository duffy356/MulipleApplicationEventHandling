package com.github.duffy356.maeh.exchanger.messages.part;

import com.github.duffy356.maeh.exchanger.CallbackState;
import com.google.gson.annotations.Expose;

/**
 * Created by doba on 02.05.2014.
 */
public class CallbackIntend {
    private String name;
    private Integer id;
    @Expose
    private CallbackState state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CallbackState getState() {
        return state;
    }

    public void setState(CallbackState state) {
        this.state = state;
    }
}
