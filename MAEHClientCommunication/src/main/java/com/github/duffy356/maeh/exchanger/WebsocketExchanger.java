package com.github.duffy356.maeh.exchanger;

import com.github.duffy356.maeh.exchanger.messages.*;
import com.github.duffy356.maeh.exchanger.messages.builder.GsonMessageBuilder;
import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

/**
 * Created by doba on 09.04.2014.
 */
@ClientEndpoint
public class WebsocketExchanger extends MAEHAbstractExchanger {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebsocketExchanger.class);
    // Websocket session -> set in Connect method
    private Session session;
    private ClientManager.ReconnectHandler reconnectHandler;
    private ClientManager clientmanager;

    private final MessageBuilder mbuilder;
    private int connectionAttempt = 1;

    public WebsocketExchanger(URI serverURI, String maehUser, String maehApplication) {
        super(serverURI, maehUser, maehApplication);
        clientmanager = ClientManager.createClient();
        mbuilder = new GsonMessageBuilder();

        // Set Reconnecthandler -> Trys to Reconnect, when connection is broken
        reconnectHandler = new ClientManager.ReconnectHandler() {

            /**
             * Called after OnClose annotated method invoked.
             *
             * When true is returned, client container will reconnect Automatically
             *
             * If Reconnect is successful -> Onopen is called
             *              not successful -> onConnectFailure gets called
             */
            @Override
            public boolean onDisconnect(CloseReason closeReason) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("### onDisconnect - state " + connectionState + ": Reconnecting... (closeReason: " + closeReason + ") reconnect count: " + connectionAttempt);
                }

                return true;
            }

            /**
             * Called when there is a connection failure
             *
             * When true is returned, client container will reconnect automatically
             *
             * This Implementation checks the retryAttempts and sends the Thread to sleep.
             * There more failed retry attempts there are, there longer the implementation waits to reconnect
             */
            @Override
            public boolean onConnectFailure(Exception exception) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("### onConnectFailure - state " + connectionState + ": Reconnecting... (reconnect count: " + connectionAttempt + ") " + exception.getMessage());
                }

                int interval = (int) MAEHAbstractExchanger.generateConnectionInterval(connectionAttempt);
                try {
                    Thread.sleep(interval);
                    LOGGER.error("Connection Reconnect-interval: " + interval);
                    connectionAttempt++;
                } catch (InterruptedException e) {
                    LOGGER.error("InterruptedException: ", e);
                }
                return true;
            }

        };

        /* Handler to Reconnect, when the connection is broken */
        clientmanager.getProperties().put(ClientManager.RECONNECT_HANDLER, reconnectHandler);
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        if (connectionState.getState() == ConnectionState.NOT_CONNECTED) {
            connectionState.setState(ConnectionState.ACQUIRE_ID);
            connectionAttempt = 1;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("onOpen ...");
            }
            this.session = session;

            // Set User and Application for the Socket on the Server
            registerClient();
        }
    }

    @OnMessage
    public synchronized void onMessage(String json) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("json received: " + json );
        }

        AbstractMAEHMessage message = mbuilder.toMessage(json);

        switch (message.getMessagetype()) {
            case REG_RES:
                acquireMAEHWebsocketId((MAEHRegistrationResponse) message);
                break;
            case INT_RES:
                acquireCallbacks((MAEHUpdateCallbackResponse) message);
                break;
            case EVT_MSG:
            case EVT_RES:
                messageReceived(message);
                break;
            default:
                throw new IllegalArgumentException("Illegal RESPONSE: " + message.getMessagetype());
        }
    }

    @OnError
    public void onError(Session s, Throwable thr) {
        // Reset the connection State
        connectionState.setState(ConnectionState.NOT_CONNECTED);

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("error occured ...", thr);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        connectionState.setState(ConnectionState.NOT_CONNECTED);

        if ( LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Session %s closed because of %s", session.getId(), closeReason));
        }
    }

    @Override
    public boolean connect() {
        if (connectionState.getState() == ConnectionState.NOT_CONNECTED) {
            try {
                clientmanager.asyncConnectToServer(this, serverURI);
                return true;
            } catch (DeploymentException e) {
                // Verbindungsaufbau war nicht erfolgreich
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Verbindungsaufbau war nicht erfolgreich", e);
                }
            }
        } else {
            throw new IllegalStateException("Already connected");
        }
        return false;
    }

    @Override
    public boolean sendTextMessage(AbstractMAEHMessage message) {
        try {
            if (connectionState.getState() != ConnectionState.NOT_CONNECTED) {
                String json = mbuilder.toJSON(message);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("sendTextMessage: " + json );
                }

                session.getAsyncRemote().sendText(json);
                return true;
            }

        } catch (IllegalStateException e) {
            // Tritt auf, wenn die Session nicht OK ist
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Senden war nicht erfolgreich", e);
            }
        }

        return false;
    }

}
