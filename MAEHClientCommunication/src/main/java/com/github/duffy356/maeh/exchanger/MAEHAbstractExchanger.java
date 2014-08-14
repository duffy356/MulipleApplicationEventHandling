package com.github.duffy356.maeh.exchanger;

import com.github.duffy356.maeh.exchanger.messages.*;
import com.github.duffy356.maeh.exchanger.messages.part.CallbackIntend;
import com.github.duffy356.maeh.exchanger.messages.part.Message;
import com.github.duffy356.maeh.exchanger.messages.part.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by doba on 16.04.2014.
 */
public abstract class MAEHAbstractExchanger {
    private final static Logger LOGGER = LoggerFactory.getLogger(MAEHAbstractExchanger.class);

    private final CopyOnWriteArrayList<Callback> callbacks;
    private Integer maehWebsocketId;
    private final String maehUser;
    private final String maehApplication;
    protected final URI serverURI;

    protected final ConnectionStateChanger connectionState;

    protected MAEHAbstractExchanger(URI serverURI, String maehUser, String maehApplication) {
        this.serverURI = serverURI;
        this.callbacks = new CopyOnWriteArrayList<Callback>();
        this.maehUser = maehUser;
        this.maehApplication = maehApplication;
        this.connectionState = new ConnectionStateChanger(ConnectionState.NOT_CONNECTED);
    }

    public Integer getMaehWebsocketId() {
        return maehWebsocketId;
    }

    public String getMaehApplication() {
        return maehApplication;
    }

    public String getMaehUser() {
        return maehUser;
    }

    public IMAEHStateChanger getStateChangerComponent() { return connectionState.getStateChanger(); }

    public void setStateChangerComponent(IMAEHStateChanger statechanger) { this.connectionState.setStateChanger(statechanger); }
    /**
     * Connect the underlying implementation to the Server with the URI of {@literal this.serverURI}
     *
     * @return If the connection was successfully established - TRUE, otherwise FALSE
     */
    public abstract boolean connect();

    /**
     * Sends a {@link java.lang.String} to the underlying WebSocket Server
     *
     * @param message - the {@link com.github.duffy356.maeh.exchanger.messages.MAEHMessageType} which gets serialized and sent
     * @return If the message was sent successful - TRUE, otherwise FALSE
     */
    protected abstract boolean sendTextMessage(AbstractMAEHMessage message);

    public synchronized void addCallback(ICallback clientCallback) {
        // Wenn ein Callback mitdemselben Namen bereits existiert, wird er vom neuen überschrieben
        for (Callback callback: this.callbacks) {
            if (clientCallback.getCallbackName().equals(callback.getName())) {
                // alten callback überschreiben
                callback = new Callback(clientCallback);
            }
        }

        Callback newCallback = new Callback(clientCallback);
        this.callbacks.add(newCallback);

        if (connectionState.getState() == ConnectionState.CONNECTED) {
            synchronizeCallbacks();
        }
    }

    public synchronized void removeCallback(ICallback clientCallback) {
        Callback callback;
        List tmpRemoveList = null;
        for (Iterator<Callback> iter = callbacks.iterator(); iter.hasNext(); ) {
            callback = iter.next();
            if (clientCallback == callback.getClientCallback()) {
                if (callback.getState() == CallbackState.SYNCHRONIZING
                        || callback.getState() == CallbackState.CONNECTED) {
                    callback.setId(-1);
                    if (callback.getState() == CallbackState.CONNECTED
                            && connectionState.getState() == ConnectionState.CONNECTED) {
                        synchronizeCallbacks();
                    }
                } else if (callback.getState() == CallbackState.NOT_CONNECTED) {
                    if (tmpRemoveList == null) {
                        tmpRemoveList = new ArrayList<Callback>();
                    }
                    tmpRemoveList.add(callback);
                } else {
                    throw new IllegalArgumentException("State not allowed" + callback.getState());
                }
                break;
            }
        }

        if (tmpRemoveList != null) {
            callbacks.removeAll(tmpRemoveList);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Callbacks size: " + callbacks.size());
            callbacks.forEach((Callback c) -> {
                LOGGER.debug(c.toString());
            });
        }
    }

    protected void acquireMAEHWebsocketId(MAEHRegistrationResponse res) {
        // The initialisation Message with the granted MAEHWebSocket Id
        this.maehWebsocketId = res.getMaehWebsocketId();

        synchronizeCallbacks();
    }

    private synchronized void synchronizeCallbacks() {
        MAEHUpdateCallbackRequest req = new MAEHUpdateCallbackRequest();
        req.setMaehWebsocketId(this.maehWebsocketId);

        if (!this.callbacks.isEmpty()) {
            for (Callback callback : this.callbacks) {
                if (callback.getState() == CallbackState.NOT_CONNECTED) {
                    if (req.getNewCallbacks() == null) {
                        req.setNewCallbacks(new ArrayList<String>());
                    }
                    callback.setState(CallbackState.SYNCHRONIZING);
                    req.getNewCallbacks().add(callback.getName());
                } else if (callback.getState() == CallbackState.CONNECTED
                                && callback.getId().intValue() == -1) {
                    if (req.getRemovedCallbacks() == null) {
                        req.setRemovedCallbacks(new ArrayList<String>());
                    }
                    callback.setState(CallbackState.SYNCHRONIZING);
                    req.getRemovedCallbacks().add(callback.getName());
                }
            }
        }

        if (req.getNewCallbacks() == null && req.getRemovedCallbacks() == null) {
            // no callbacks to change -> connected
            this.connectionState.setState(ConnectionState.CONNECTED);
        } else {
            // Send the current callback to the Server and handle them
            if (this.connectionState.getState() == ConnectionState.ACQUIRE_ID) {
                this.connectionState.setState(ConnectionState.UPDATE_CALLBACKS);
            }

            sendTextMessage(req);
        }
    }

    protected synchronized void acquireCallbacks(MAEHUpdateCallbackResponse res) {
        Callback callback;
        Iterator<Callback> iter;
        List tmpRemoveList = null;

        for (iter = callbacks.iterator(); iter.hasNext(); ) {
            callback = iter.next();
            if (callback.getState() == CallbackState.SYNCHRONIZING) {
                if (callback.getId() == null
                        && res.getNewCallbacks() != null) {
                    // id = null -> muss bei den registrierten callbacks dabei sein
                    for (CallbackIntend newCallback : res.getNewCallbacks()) {
                        if (newCallback.getName().equals(callback.getName())) {
                            // gelieferte Id übernehmen
                            callback.setId(newCallback.getId());
                            callback.setState(CallbackState.CONNECTED);
                            break;
                        }
                    }
                } else if (callback.getId() != null
                                && callback.getId().intValue() == -1
                                && res.getRemovedCallbacks() != null) {
                    // id == -1 -> muss bei den gelöschten intends dabei sein
                    for (CallbackIntend removedCallbackIntend : res.getRemovedCallbacks()) {
                        if (removedCallbackIntend.getName().equals(callback.getName())) {
                            // Callback entfernen
                            if (tmpRemoveList == null) {
                                tmpRemoveList = new ArrayList<Callback>();
                            }
                            tmpRemoveList.add(callback);
                            break;
                        }
                    }
                }
            }
        }

        // remove Items
        if (tmpRemoveList != null) {
            callbacks.removeAll(tmpRemoveList);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Callbacks size: " + callbacks.size());
            callbacks.forEach((Callback c) -> {
                LOGGER.debug(c.toString());
            });
        }

        this.connectionState.setState(ConnectionState.CONNECTED);
    }

    protected void messageReceived(AbstractMAEHMessage message) {
        if (MessageType.EVT_MSG == message.getMessagetype()) {
            MAEHEventMessage eventMessage = (MAEHEventMessage) message;
            callbacks.forEach((Callback receiver) -> {

                if (receiver.getName().equals(eventMessage.getCallback())) {
                    LOGGER.debug("found: " + receiver.getName());

                    new Thread(
                            () -> receiver.getClientCallback().messageReceived(eventMessage.getData(), eventMessage.getSenderId())
                    ).start();
                }
            });
        } else if (MessageType.EVT_RES == message.getMessagetype()) {
            MAEHEventResponse eventResponse = (MAEHEventResponse) message;
            callbacks.forEach((Callback receiver) -> {

                if (receiver.getName().equals(eventResponse.getCallback())) {
                    LOGGER.debug("found: " + receiver.getName());

                    new Thread(
                            () -> receiver.getClientCallback().messageSent(eventResponse.getReceivers())
                    ).start();
                }
            });
        }
    }

    /**
     * Registers this MAEHExchangerProperties on the SocketServer
     * The granted Id of this exchanger gets invalid and the properties
     * of the current Object are Composed to a RegisterMessage and this message gets sent.
     * The Server returns the new MaehWebsocketId.
     */
    protected void registerClient() {
        if (this.maehUser == null
                || this.maehApplication == null) {
            throw new IllegalArgumentException();
        }
        // Reset the Id of the connected Websocket
        this.maehWebsocketId = null;
        // Reset registered Callbacks
        for (Callback callback : this.callbacks) {
            callback.setId(null);
            callback.setState(CallbackState.NOT_CONNECTED);
        }

        MAEHRegistrationRequest req = new MAEHRegistrationRequest();
        req.setMaehUser(this.maehUser);
        req.setMaehApplication(this.maehApplication);

        sendTextMessage(req);
    }

    public void sendMAEHMessage(Message message) {
        if (!message.getData().isEmpty()) {
            MAEHEventRequest req = new MAEHEventRequest(message);

            sendTextMessage(req);
        }
    }

    /**
     * Return a new {@link com.github.duffy356.maeh.exchanger.messages.part.Receiver} instance
     * based on the properties of this Exchanger
     *
     * @param receivertype The type of the generated Receiver
     * @param intend A intend of the receiver
     * @return the generated {@link com.github.duffy356.maeh.exchanger.messages.part.Receiver} instance
     */
    public Receiver generateReceiver(ReceiverType receivertype, String intend) {
        Receiver receiver = new Receiver();
        receiver.setReceivertype(receivertype);
        receiver.setCallback(intend);
        switch (receivertype) {
            case BROADCAST:
                break;
            case USER:
                receiver.setUsername(this.maehUser);
                break;
            case APPLICATION:
                receiver.setApplication(this.maehApplication);
                break;
            case USERAPPLICATION:
                receiver.setUsername(this.maehUser);
                receiver.setApplication(this.maehApplication);
                break;
            default:
                throw new IllegalArgumentException("ReceiverType: " + receivertype + "is not supported!");
        }
        return receiver;
    }

    /**
     * @return The current State of the Connection
     */
    public ConnectionState getConnectionState() {
        return connectionState.getState();
    }

    /**
     * Calculate the Interval for the next connection Attempt
     *
     * @param attempt The Number of the current Connection Attempt
     * @return The Interval for the next connection Attempt
     */
     public static double generateConnectionInterval(int attempt) {
         double maxInterval = (Math.pow(2, attempt) - 1) * 1000;

         // Set Maximum of Interval to 60 Seconds
         if (maxInterval > 60 * 1000) {
             maxInterval = 60 * 1000;
         }

         // Generate number between 0 and 1 and multiply with maxInterval
         return Math.random() * maxInterval;
     }


}
