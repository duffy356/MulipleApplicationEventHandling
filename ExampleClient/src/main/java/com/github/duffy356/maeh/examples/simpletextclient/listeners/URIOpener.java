package com.github.duffy356.maeh.examples.simpletextclient.listeners;

import com.github.duffy356.maeh.examples.simpletextclient.guiinterfaces.ErrorHandler;
import com.github.duffy356.maeh.exchanger.ICallback;
import com.github.duffy356.maeh.exchanger.MAEHAbstractExchanger;
import com.github.duffy356.maeh.exchanger.messages.ReceiverType;
import com.github.duffy356.maeh.exchanger.messages.part.Message;
import com.github.duffy356.maeh.exchanger.messages.part.Receiver;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by doba on 08.04.2014.
 */
public class URIOpener implements ActionListener, ICallback {
    private final static Logger LOGGER = Logger.getLogger(URIOpener.class);

    private JTextComponent uriTextComponent;
    private final MAEHAbstractExchanger endpoint;
    private ErrorHandler errorPane;
    private JTextArea messageArea;

    public URIOpener(MAEHAbstractExchanger endpoint, JTextField uriField, JTextArea allMessages) {
        this.endpoint = endpoint;
        this.uriTextComponent = uriField;
        this.errorPane = (ErrorHandler) SwingUtilities.windowForComponent(uriTextComponent);
        this.endpoint.addCallback(this);
        this.messageArea = allMessages;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Validate the entered URI
        try {
            URI ziel = new URI(uriTextComponent.getText());

            LOGGER.debug("Uri: " + ziel);
            Message message = new Message(this);
            message.putMessage("uriTextComponent", ziel.toString());

            Receiver uriReceiver = new Receiver();
            uriReceiver.setReceivertype(ReceiverType.USERAPPLICATION);
            uriReceiver.setCallback(getCallbackName());
            uriReceiver.setApplication("testframe");
            uriReceiver.setUsername("testuser123");
            message.addReceiver(uriReceiver);

            endpoint.sendMAEHMessage(message);

        } catch (URISyntaxException e1) {
            errorPane.setErrorMessage(e1.getReason());
        }
    }

    @Override
    public void messageReceived(Map<String, Object> s, Integer senderId) {
        // Hide and destroy the window
        JFrame window = (JFrame) SwingUtilities.windowForComponent(uriTextComponent);

        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
//        window.dispose();

    }

    @Override
    public void messageSent(Integer receivers) {
        String msg = receivers + " Clients opened the Requested Page!";
        LOGGER.info(msg);
        if (messageArea.getText() != null
                && messageArea.getText().length() > 0)
            messageArea.append("\n");

        messageArea.append(msg);
    }

    @Override
    public String getCallbackName() {
        return "test.uriopener";
    }
}