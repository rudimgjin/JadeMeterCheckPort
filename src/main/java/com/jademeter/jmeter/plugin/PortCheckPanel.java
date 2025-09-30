package com.jademeter.jmeter.plugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PortCheckPanel extends JPanel {
    private final JTextField serverField;
    private final JTextField portField;
    private final JTextArea logArea;
    private final JButton checkButton;

    public PortCheckPanel() {
        setLayout(new BorderLayout(5, 5));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Server IP:"));
        serverField = new JTextField(12);
        inputPanel.add(serverField);

        inputPanel.add(new JLabel("Port:"));
        portField = new JTextField(5);
        inputPanel.add(portField);

        checkButton = new JButton("Check Port");
        inputPanel.add(checkButton);

        add(inputPanel, BorderLayout.NORTH);

        logArea = new JTextArea(12, 40);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkButton.setText("Checking...");
                checkButton.setEnabled(false);
                logArea.setText("");
                new Thread(() -> performCheck()).start();
            }
        });
    }

    private void performCheck() {
        String server = serverField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            appendLog("Invalid port number");
            resetButton();
            return;
        }

        appendLog("Check start: " + server + ":" + port);

        // Step 1: Ping check
        boolean pingSuccess = false;
        try {
            InetAddress address = InetAddress.getByName(server);
            pingSuccess = address.isReachable(3000);
            appendLog("Ping: " + (pingSuccess ? "Success [V]" : "Failed [X]"));
        } catch (IOException e) {
            appendLog("Ping: Failed [X]");
        }

        // Step 2: TCP port check
        boolean tcpSuccess = false;
        try (Socket socket = new Socket(server, port)) {
            tcpSuccess = true;
            appendLog("TCP connect: Success [V]");
        } catch (IOException e) {
            appendLog("TCP connect: Failed [X]");
        }

        // Step 3: Diagnosis
        if (!pingSuccess) {
            appendLog("Diagnosis: Server unreachable");
        } else if (!tcpSuccess) {
            appendLog("Diagnosis: Possible firewall or blocked port");
        } else {
            appendLog("Diagnosis: Port open and reachable");
        }

        appendLog("Check complete");
        resetButton();
    }

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void resetButton() {
        SwingUtilities.invokeLater(() -> {
            checkButton.setText("Complete & Recheck");
            checkButton.setEnabled(true);
        });
    }
}
