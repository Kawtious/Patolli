/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.game.online;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import patolli.game.online.server.threads.SocketStreams;
import patolli.utils.Console;

public class Client {

    private DataInputStream dis;

    private DataOutputStream dos;

    private String ip;

    private int port;

    private final String SECRET_KEY = "sT8w69pzFbuK";

    private volatile boolean connected = false;

    private static Client instance;

    /**
     * Singleton pattern to keep a single instance of this class program running
     *
     * @return The instance of the program is returned, if there's none a new one is created
     */
    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }

        return instance;
    }

    /**
     *
     */
    private Client() {
    }

    /**
     *
     * @return
     */
    public boolean run() {
        if (ip.isEmpty()) {
            return false;
        }

        if (port < 0 || port > 65535) {
            return false;
        }

        return connect(ip, port);
    }

    /**
     *
     * @param ip
     * @param port
     * @return
     */
    private boolean connect(final String ip, final int port) {
        try (final Socket socket = new Socket(ip, port)) {
            try (final InputStream in = socket.getInputStream()) {
                try (final OutputStream out = socket.getOutputStream()) {
                    dis = new DataInputStream(in);
                    dos = new DataOutputStream(out);

                    receive();

                    if (connected) {
                        final Thread listen = listen();
                        listen.setDaemon(true);
                        listen.start();

                        final Thread write = write();
                        write.setDaemon(true);
                        write.start();
                    }

                    while (connected) {
                    }

                    Console.Error.WriteLine("Client", "Disconnected from server");

                    return true;
                }
            }
        } catch (final IOException ex) {
            Console.Error.WriteLine("Client", "Disconnected: " + ex.getMessage());
        } finally {
            try {
                dis.close();
                dos.close();
            } catch (IOException ex) {
            }
        }

        return false;
    }

    /**
     *
     * @throws IOException
     */
    private void receive() throws IOException {
        byte[] message = SocketStreams.readBytes(dis);

        if (!connected) {
            if (verify(message)) {
                Console.WriteLine("Client", "Connected to " + ip + ":" + port);
                connected = true;
            }
        } else {
            Console.WriteLine("Client", new String(message));
        }
    }

    private boolean verify(byte[] message) {
        if (Arrays.equals(message, SECRET_KEY.getBytes())) {
            send(SECRET_KEY.getBytes());
            return true;
        }

        return false;
    }

    /**
     *
     * @return
     */
    private Thread listen() {
        return new Thread(() -> {
            while (connected) {
                try {
                    receive();
                } catch (IOException ex) {
                    disconnect();
                }
            }
        });
    }

    /**
     *
     * @return
     */
    private Thread write() {
        return new Thread(() -> {
            try (final Scanner scanner = new Scanner(System.in)) {
                while (connected) {
                    byte[] message = scanner.nextLine().getBytes();
                    if (message != null && message.length > 0) {
                        send(message);
                    }
                }
            }
        });
    }

    /**
     *
     * @param message
     */
    private void send(final byte[] message) {
        try {
            SocketStreams.sendBytes(dos, message);
        } catch (IOException ex) {
        }
    }

    /**
     *
     */
    private void disconnect() {
        connected = false;
    }

    /**
     *
     * @param ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

}
