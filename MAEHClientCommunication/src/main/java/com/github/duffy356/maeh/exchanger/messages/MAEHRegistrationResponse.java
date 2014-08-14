package com.github.duffy356.maeh.exchanger.messages;

/**
 * Created by doba on 02.05.2014.
 */
public class MAEHRegistrationResponse extends AbstractMAEHMessage {
    private Integer maehWebsocketId;

    public MAEHRegistrationResponse() {
        super(MessageType.REG_RES);
    }

    public Integer getMaehWebsocketId() {
        return maehWebsocketId;
    }

    public void setMaehWebsocketId(Integer maehWebsocketId) {
        this.maehWebsocketId = maehWebsocketId;
    }
}
