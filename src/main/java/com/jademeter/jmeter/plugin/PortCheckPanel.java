package com.jademeter.jmeter.plugin;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PortCheckPanel extends JPanel {
    private final JTextField serverField;
    private final JTextField portField;
    private final JTextField timeoutField;
    private final JComboBox<String> protocolBox;
    private final JTextArea logArea;
    private final JButton checkButton;

    public PortCheckPanel() {
        setLayout(new BorderLayout(5, 5));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        inputPanel.add(new JLabel("Server:"));
        serverField = new JTextField(12);
        inputPanel.add(serverField);

        inputPanel.add(new JLabel("Port:"));
        portField = new JTextField(5);
        inputPanel.add(portField);

        inputPanel.add(new JLabel("Protocol:"));
        protocolBox = new JComboBox<>(new String[]{"HTTP", "HTTPS"});
        inputPanel.add(protocolBox);

        inputPanel.add(new JLabel("Timeout(ms):"));
        timeoutField = new JTextField("3000", 5);
        inputPanel.add(timeoutField);

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
        int timeout;
        String protocol = (String) protocolBox.getSelectedItem();
        boolean useSSL = "HTTPS".equalsIgnoreCase(protocol);

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            appendLog("Invalid port number");
            resetButton();
            return;
        }

        try {
            timeout = Integer.parseInt(timeoutField.getText().trim());
        } catch (NumberFormatException e) {
            appendLog("Invalid timeout value");
            resetButton();
            return;
        }

        appendLog("Check start: " + server + ":" + port + " [" + protocol + "]");

        // Step 1: Ping check
        boolean pingSuccess = false;
        try {
            InetAddress address = InetAddress.getByName(server);
            pingSuccess = address.isReachable(timeout);
            appendLog("Ping: " + (pingSuccess ? "Success [V]" : "Failed [X]"));
        } catch (IOException e) {
            appendLog("Ping: Failed [X]");
        }

        // Step 2: TCP / SSL check (Ping 실패 여부와 상관없이 수행)
        boolean tcpSuccess = false;
        boolean sslHandshakeFailed = false;
        try {
            if (useSSL) {
                try (SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket()) {
                    sslSocket.connect(new InetSocketAddress(server, port), timeout);
                    sslSocket.startHandshake(); // SSL 핸드셰이크 확인
                    tcpSuccess = true;
                }
            } else {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(server, port), timeout);
                    tcpSuccess = true;
                }
            }
        } catch (IOException e) {
            tcpSuccess = false;
            if (useSSL) {
                sslHandshakeFailed = true;
            }
        }
        appendLog("TCP connect: " + (tcpSuccess ? "Success [V]" : "Failed [X]"));

        // Step 3: Diagnosis
        if (!pingSuccess) {
            appendLog("Diagnosis: Ping failed, TCP " + (tcpSuccess ? "success" : "failed"));
            if (tcpSuccess && useSSL && sslHandshakeFailed) {
                appendLog("Diagnosis: Port open but SSL handshake failed");
            } else if (!tcpSuccess) {
                appendLog("Diagnosis: Possible firewall or blocked port");
            }
        } else if (!tcpSuccess) {
            if (sslHandshakeFailed) {
                appendLog("Diagnosis: Port open but SSL handshake failed");
            } else {
                appendLog("Diagnosis: Possible firewall or blocked port");
            }
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
