package com.github.duffy356.maeh.exchanger.messages;

/**
 * Created by doba on 02.05.2014.
 */
public class MAEHRegistrationRequest extends AbstractMAEHMessage {
    private String maehUser;
    private String maehApplication;

    public MAEHRegistrationRequest() {
        super(MessageType.REG_REQ);
    }

    public String getMaehUser() {
        return maehUser;
    }

    public void setMaehUser(String maehUser) {
        this.maehUser = maehUser;
    }

    public String getMaehApplication() {
        return maehApplication;
    }

    public void setMaehApplication(String maehApplication) {
        this.maehApplication = maehApplication;
    }
}
