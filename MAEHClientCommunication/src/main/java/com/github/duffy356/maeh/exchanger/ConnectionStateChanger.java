package com.github.duffy356.maeh.exchanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by doba on 08.05.2014.
 */
class ConnectionStateChanger {
    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectionStateChanger.class);
    private IMAEHStateChanger stateChanger;
    private volatile ConnectionState state;

    ConnectionStateChanger(ConnectionState initialState) {
        this.state = initialState;
    }

    IMAEHStateChanger getStateChanger() {
        return stateChanger;
    }

    void setStateChanger(IMAEHStateChanger stateChanger) {
        this.stateChanger = stateChanger;
    }

    synchronized ConnectionState getState() {
        return state;
    }

    synchronized void setState(ConnectionState state) {
        LOGGER.debug("setState: " + state);
        this.state = state;
        if (stateChanger != null) {
            new Thread(
                    () -> stateChanger.stateChanged(this.state)
            ).start();
        }
    }
}
