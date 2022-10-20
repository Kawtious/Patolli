/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.game.online;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import patolli.utils.Console;
import patolli.utils.SocketHelper;

public class Client {

    private byte[] key = "$31$".getBytes();

    private DataInputStream dis;

    private DataOutputStream dos;

    private String ip;

    private int port;

    private volatile boolean connected = false;

    private String[] args;

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
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            receive();

            if (connected) {
                final Thread listen = listen();
                listen.setDaemon(true);
                listen.start();

                final Thread write = write();
                write.setDaemon(true);
                write.start();

                if (args.length > 0) {
                    for (String arg : args) {
                        send(arg.getBytes());
                    }
                }
            }

            while (connected) {
            }

            Console.Error.WriteLine("Client", "Disconnected from server");

            dis.close();
            dos.close();
            return true;
        } catch (final IOException ex) {
            Console.Error.WriteLine("Client", "Disconnected: " + ex.getMessage());
        }

        return false;
    }

    /**
     *
     * @throws IOException
     */
    private void receive() throws IOException {
        if (!connected) {
            setup(SocketHelper.readBytes(dis, key));
            Console.WriteLine("Client", "Connected to " + ip + ":" + port);
            connected = true;
        } else {
            byte[] message = SocketHelper.readBytes(dis, key);
            Console.WriteLine("Client", new String(message));
        }
    }

    private void setup(byte[] key) {
        this.key = Arrays.copyOf(key, key.length);
        send(key);
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
    private void send(byte[] message) {
        try {
            SocketHelper.sendBytes(dos, message, key);
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

    /**
     *
     * @return
     */
    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    /**
     *
     * @param args
     */
    public void setArgs(String[] args) {
        this.args = Arrays.copyOf(args, args.length);
    }

}
