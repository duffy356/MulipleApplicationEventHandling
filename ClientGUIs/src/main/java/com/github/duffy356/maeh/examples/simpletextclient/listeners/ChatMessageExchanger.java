package com.github.duffy356.maeh.examples.simpletextclient.listeners;

import com.github.duffy356.maeh.examples.simpletextclient.maehinterfaces.MAEHExchangerHolder;
import com.github.duffy356.maeh.exchanger.ICallback;
import com.github.duffy356.maeh.exchanger.MAEHAbstractExchanger;
import com.github.duffy356.maeh.exchanger.messages.part.Message;
import com.github.duffy356.maeh.exchanger.messages.ReceiverType;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Created by doba on 08.04.2014.
 */
public class ChatMessageExchanger implements ActionListener, ICallback {
    private final static Logger LOGGER = Logger.getLogger(ChatMessageExchanger.class);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private JTextComponent sendComponent;
    private JTextArea receiverComponent;
    private MAEHAbstractExchanger clientEndpoint;

    public ChatMessageExchanger(JTextComponent sender, JTextArea receiver) {
        this.sendComponent = sender;
        this.receiverComponent = receiver;
        this.clientEndpoint = ((MAEHExchangerHolder) SwingUtilities.windowForComponent(sendComponent)).getMAEHExchanger();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = sendComponent.getText();

        if (receiverComponent.getText() != null
                && receiverComponent.getText().length() > 0)
            receiverComponent.append("\n");

//        LocalDateTime dateTime = LocalDateTime.now().format(formatter);
        String sendText = LocalDateTime.now().format(formatter)
                            + " "
                            + clientEndpoint.getMaehUser()
                            + ": "
                            + text;
        receiverComponent.append(sendText);

        LOGGER.debug("Text to send:" + sendText);
        Message maehMessage = new Message(this);
        maehMessage.putMessage("message", sendText);
        maehMessage.addReceiver(clientEndpoint.generateReceiver(ReceiverType.BROADCAST, getCallbackName()));

        clientEndpoint.sendMAEHMessage(maehMessage);
    }

    @Override
    public void messageReceived(Map<String, Object> map, Integer senderId) {
        LOGGER.debug("received Data");
        if (map.containsKey("message")) {
            if (receiverComponent.getText() != null
                    && receiverComponent.getText().length() > 0)
                receiverComponent.append("\n");


            receiverComponent.append(map.get("message").toString());
        }
    }

    @Override
    public void messageSent(Integer receivers) {
        LOGGER.debug("Eventmessage sent");
        if (receiverComponent.getText() != null
                && receiverComponent.getText().length() > 0)
            receiverComponent.append("\n");


        receiverComponent.append("Eventmessage was sent to " + receivers + " clients!");
    }

    @Override
    public String getCallbackName() {
        return "chatmessage";
    }
}