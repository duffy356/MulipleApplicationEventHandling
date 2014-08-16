package com.github.duffy356.maeh.examples.simpletextclient.listeners;

import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by doba on 08.04.2014.
 */
public class ClearTextListener implements ActionListener {
    private JTextComponent textComponent;

    public ClearTextListener(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        textComponent.setText(null);
    }
}
