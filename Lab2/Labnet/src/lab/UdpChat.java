package lab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpChat extends JFrame {

    private static final int PORT_A = 5000;
    private static final int PORT_B = 5001;
    private static final int MAX_MESSAGE_BYTES = 1024;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DatagramSocket socket;
    private final InetAddress peerAddress;
    private final int peerPort;
    private final BlockingQueue<String> outbox = new LinkedBlockingQueue<>();

    private final JTextArea log = new JTextArea(16, 42);
    private final JTextField input = new JTextField();
    private final JButton sendButton = new JButton("Send");

    private final Thread senderThread = new Thread(this::senderLoop, "udp-chat-sender");
    private final Thread receiverThread = new Thread(this::receiverLoop, "udp-chat-receiver");
    private volatile boolean running = true;

    public UdpChat(int localPort, String peerHost, int peerPort) throws IOException {
        super("UDP Chat");
        checkPort(localPort, "local port");
        checkPort(peerPort, "peer port");
        this.peerAddress = InetAddress.getByName(peerHost); // resolve before binding
        this.peerPort = peerPort;
        this.socket = new DatagramSocket(localPort);        // e.g. BindException if port is taken

        setTitle("UDP Chat - listening on :" + localPort + "  ->  " + peerHost + ":" + peerPort);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(5, 5));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(new JScrollPane(log), BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);

        sendButton.addActionListener(e -> send());
        input.addActionListener(e -> send()); // Enter key

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                shutdown();
            }
        });

        pack();
        setLocation(80 + (localPort % 2) * 520, 120); // two windows on one PC don't overlap

        senderThread.setDaemon(true);
        receiverThread.setDaemon(true);
        receiverThread.start();
        senderThread.start();

        log("[system] ready. Listening on UDP port " + localPort + ", sending to "
                + peerAddress.getHostAddress() + ":" + peerPort);
    }

    private static void checkPort(int port, String what) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid " + what + ": " + port + " (use 1-65535)");
        }
    }

    /** Called on the Swing thread: just hands the text over to the sender thread. */
    private void send() {
        String text = input.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        if (text.getBytes(StandardCharsets.UTF_8).length > MAX_MESSAGE_BYTES) {
            log("[system] message too long (max " + MAX_MESSAGE_BYTES + " bytes), not sent");
            return;
        }
        input.setText("");
        outbox.add(text);
        input.requestFocusInWindow();
    }

    /** Sender thread: waits for messages in the queue and sends each one as a datagram. */
    private void senderLoop() {
        try {
            while (running) {
                String text = outbox.take();
                byte[] data = text.getBytes(StandardCharsets.UTF_8);
                try {
                    socket.send(new DatagramPacket(data, data.length, peerAddress, peerPort));
                    log("Me: " + text);
                } catch (IOException e) {
                    if (running) {
                        log("[error] could not send: " + e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Receiver thread: blocks in receive() and shows every datagram that arrives. */
    private void receiverLoop() {
        byte[] buf = new byte[4096];
        while (running && !socket.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                String text = new String(packet.getData(), packet.getOffset(),
                        packet.getLength(), StandardCharsets.UTF_8);
                log("Peer: " + text);
            } catch (IOException e) {
                if (running && !socket.isClosed()) {
                    log("[error] receive failed: " + e.getMessage());
                }
            }
        }
    }

    /** Safe to call from any thread: appends one timestamped line to the message window. */
    private void log(String line) {
        final String stamped = "[" + LocalTime.now().format(TIME) + "] " + line + "\n";
        SwingUtilities.invokeLater(() -> {
            log.append(stamped);
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    private void shutdown() {
        running = false;
        socket.close();             // unblocks receive()
        senderThread.interrupt();   // unblocks outbox.take()
    }

    // startup
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> start(args));
    }

    private static void start(String[] args) {
        try {
            int localPort;
            String peerHost;
            int peerPort;

            if (args.length == 3) {
                localPort = Integer.parseInt(args[0].trim());
                peerHost = args[1].trim();
                peerPort = Integer.parseInt(args[2].trim());
            } else {

                boolean first = isPortFree(PORT_A);
                JTextField lp = new JTextField(String.valueOf(first ? PORT_A : PORT_B), 10);
                JTextField ph = new JTextField("localhost", 10);
                JTextField pp = new JTextField(String.valueOf(first ? PORT_B : PORT_A), 10);
                JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
                form.add(new JLabel("My port (listen):"));
                form.add(lp);
                form.add(new JLabel("Peer IP / host:"));
                form.add(ph);
                form.add(new JLabel("Peer port:"));
                form.add(pp);
                int answer = JOptionPane.showConfirmDialog(null, form, "UDP Chat - setup",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (answer != JOptionPane.OK_OPTION) {
                    return;
                }
                localPort = Integer.parseInt(lp.getText().trim());
                peerHost = ph.getText().trim();
                peerPort = Integer.parseInt(pp.getText().trim());
            }

            UdpChat chat = new UdpChat(localPort, peerHost, peerPort);
            chat.setVisible(true);
            chat.input.requestFocusInWindow();
        } catch (NumberFormatException e) {
            error("Ports must be numbers.");
        } catch (SocketException e) {
            error("Cannot open the local port (is it already used by another terminal?)\n"
                    + e.getMessage());
        } catch (Exception e) {
            error(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    private static boolean isPortFree(int port) {
        try (DatagramSocket probe = new DatagramSocket(port)) {
            return true;
        } catch (SocketException e) {
            return false;
        }
    }

    private static void error(String message) {
        JOptionPane.showMessageDialog(null, message, "UDP Chat", JOptionPane.ERROR_MESSAGE);
    }
}
