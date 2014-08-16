package com.github.duffy356.maeh.examples.simpletextclient.listeners;

import com.github.duffy356.maeh.exchanger.ConnectionState;
import com.github.duffy356.maeh.exchanger.IMAEHStateChanger;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Created by doba on 07.05.2014.
 */
public class LabelStateChanger implements IMAEHStateChanger {
    private final static Logger LOGGER = Logger.getLogger(LabelStateChanger.class);
    private JLabel component;

    public LabelStateChanger(JLabel component) {
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }

        this.component = component;
    }

    public JLabel getComponent() {
        return component;
    }

    public void setComponent(JLabel component) {
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }
        Color color = null;
        if (this.component != null) {
            // RESET Color
            color = this.component.getForeground();
            this.component.setForeground(Color.BLACK);
        }
        this.component = component;
        if (color != null) {
            this.component.setForeground(color);
        }
    }

    @Override
    public void stateChanged(ConnectionState newState) {
        LOGGER.debug("stateChanged");
        switch (newState) {
            case NOT_CONNECTED:
                this.component.setForeground(Color.RED);
                this.component.setText("Not Connected");
                break;
            case ACQUIRE_ID:
                this.component.setForeground(Color.ORANGE);
                this.component.setText("Register Client");
                break;
            case CONNECTED:
                this.component.setForeground(new Color(0x009900));
                this.component.setText("Connected");
                break;
            case UPDATE_CALLBACKS:
                this.component.setForeground(Color.YELLOW);
                this.component.setText("Update Callbacks");
                break;
            default:
                this.component.setForeground(Color.BLACK);
                this.component.setText("Invalid State");

        }
    }
}
