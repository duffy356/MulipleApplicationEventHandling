package com.github.duffy356.maeh.examples.simpletextclient;

import com.github.duffy356.maeh.examples.simpletextclient.guiinterfaces.ErrorHandler;
import com.github.duffy356.maeh.examples.simpletextclient.listeners.URIOpener;
import com.github.duffy356.maeh.exchanger.MAEHAbstractExchanger;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by doba on 02.05.2014.
 */
public class WebPageOpener extends JFrame implements ErrorHandler{
    private JPanel mainpanel;
    private JTextField urifield;
    private JButton openButton;
    private JPanel actionpanel;
    private JPanel errorpanel;
    private JLabel errorline;

    private final MAEHAbstractExchanger endpoint;

    public WebPageOpener(MAEHAbstractExchanger endpoint, final JTextArea allMessages) {
        super("Webpageopener");
        this.endpoint = endpoint;

        getContentPane().add(mainpanel);

        URIOpener uriListener = new URIOpener(endpoint, urifield, allMessages);

        urifield.setText("www.w3schools.com");

        openButton.addActionListener(uriListener);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            // remove intend from socket
            endpoint.removeCallback(uriListener);

            super.windowClosed(e);
            }
        });

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        pack();
        setVisible(true);
    }

    @Override
    public void resetError() {
        errorline.setText(null);
    }

    @Override
    public void setErrorMessage(String message) {
        errorline.setText(message);
    }
}
