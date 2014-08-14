package com.github.duffy356.maeh.exchanger;

/**
 * Created by doba on 07.05.2014.
 */
class Callback {
    private ICallback clientCallback;
    private String name;
    private Integer id;
    private CallbackState state;

    Callback(ICallback clientCallback) {
        this.clientCallback = clientCallback;
        this.name = clientCallback.getCallbackName();
        this.id = null;
        this.state = CallbackState.NOT_CONNECTED;
    }

    @Override
    public String toString() {
        return "Callback{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", state=" + state +
                '}';
    }

    public String getName() {
        return name;
    }

    public ICallback getClientCallback() {
        return clientCallback;
    }

    public void setState(CallbackState state) {
        this.state = state;
    }

    public CallbackState getState() {
        return state;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
