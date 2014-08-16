package com.github.duffy356.maeh.examples.simpletextclient;

import com.github.duffy356.maeh.examples.simpletextclient.listeners.ChatMessageExchanger;
import com.github.duffy356.maeh.examples.simpletextclient.listeners.LabelStateChanger;
import com.github.duffy356.maeh.examples.simpletextclient.maehinterfaces.MAEHExchangerHolder;
import com.github.duffy356.maeh.examples.simpletextclient.listeners.ClearTextListener;
import com.github.duffy356.maeh.exchanger.WebsocketExchanger;
import com.github.duffy356.maeh.exchanger.MAEHAbstractExchanger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.websocket.DeploymentException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * Created by doba on 30.03.2014.
 */

public class SimpleTextClient extends JFrame implements MAEHExchangerHolder {
    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleTextClient.class);
    private JPanel mainpanel;
    private JButton clear;
    private JTextField message;
    private JPanel upperPanel;
    private JPanel lowerPanel;
    private JButton sendButton;
    private JLabel text;
    private JTextArea allMessages;
    private JLabel stateLabel;

    private final MAEHAbstractExchanger maehExchanger;

    private final String userId = "testuser123";
    private final String applicationId = "JavaApp";

    public static void main(String[] args) throws IOException, DeploymentException {
        // Setup Log4j Properties
        PropertyConfigurator.configure(Loader.getResource("log4j.properties"));

        // load Properties
        Properties systemProperties = new Properties();
        InputStream is = SimpleTextClient.class.getClassLoader().getResourceAsStream("system.properties");
        systemProperties.load(is);
        String uri = systemProperties.getProperty("host");

        // start GUI
        SwingUtilities.invokeLater(() -> new SimpleTextClient(uri));
        LOGGER.debug("Application started");
    }

    public SimpleTextClient(String uri) {
        super("SimpleTextClient");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* Set the MENU */
        JMenuBar menubar = new JMenuBar();
        JMenu openMenu = new JMenu("Open");
        JMenuItem openWebPageOpener = new JMenuItem("open URI Sender");
        openWebPageOpener.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WebPageOpener submask = new WebPageOpener(maehExchanger, allMessages);
            }
        });
        openMenu.add(openWebPageOpener);
        menubar.add(openMenu);
        setJMenuBar(menubar);

        getContentPane().add(mainpanel);

//        allMessages.setText("Some Initial Testtext");
        clear.addActionListener(new ClearTextListener(allMessages));

        // initialize the MAEHExchanger
        this.maehExchanger = new WebsocketExchanger(URI.create(uri), userId, applicationId);

        // Set StateChanger Component
        this.maehExchanger.setStateChangerComponent(new LabelStateChanger(stateLabel));

        // Create Callback
        ChatMessageExchanger chatmessageExchanger = new ChatMessageExchanger(message, allMessages);
        sendButton.addActionListener(chatmessageExchanger);

        // Add Callback to Exchanger
        maehExchanger.addCallback(chatmessageExchanger);

        // connect to Websocket server
        this.maehExchanger.connect();

        pack();
        setVisible(true);
    }

    @Override
    public MAEHAbstractExchanger getMAEHExchanger() {
        return maehExchanger;
    }
}
