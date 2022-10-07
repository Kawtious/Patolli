/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import patolli.game.Player;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;
import patolli.game.online.server.ServerManager;
import patolli.game.utils.Console;

public abstract class SocketThread extends Thread {

    private final Socket socket;

    private DataInputStream dis;

    private DataOutputStream dos;

    private Group group;

    private Channel channel;

    private Player player;

    private volatile boolean connected = false;

    private byte[] SECRET_KEY = "sT8w69pzFbuK".getBytes();

    public final ServerManager server = ServerManager.getInstance();

    /**
     *
     * @param socket
     * @param player
     * @throws IOException
     */
    public SocketThread(final Socket socket, final Player player) throws IOException {
        this.player = player;
        this.socket = socket;
    }

    /**
     *
     */
    @Override
    public void run() {
        try (final InputStream in = socket.getInputStream()) {
            try (final OutputStream out = socket.getOutputStream()) {
                dis = new DataInputStream(in);
                dos = new DataOutputStream(out);

                validate();

                while (connected) {
                    listen();
                }

                Console.WriteLine("SocketThread", "[" + player.getName() + "] disconnected from server");

                close();
            }
        } catch (final IOException ex) {
            Console.Error.WriteLine("SocketThread", "[" + player.getName() + "] Disconnected: " + ex.getMessage());
        } finally {
            try {
                dis.close();
                dos.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     *
     */
    private void validate() throws IOException {
        send(SECRET_KEY);

        if (!Arrays.equals(listen(), SECRET_KEY)) {
            disconnect();
        } else {
            Console.WriteLine("SocketThread", socket.getInetAddress() + " has connected");
            connected = true;
        }
    }

    /**
     *
     * @return
     */
    private byte[] listen() {
        try {
            byte[] input = SocketStreams.readBytes(dis);

            if (connected) {
                execute(input);
            }

            return input;
        } catch (final IOException ex) {
            disconnect();
        }

        return new byte[1];
    }

    /**
     *
     * @param msg
     * @throws IOException
     */
    public abstract void execute(final byte[] msg) throws IOException;

    /**
     *
     * @param message
     */
    public void send(final byte[] message) {
        try {
            SocketStreams.sendBytes(dos, message);
        } catch (IOException ex) {
        }
    }

    /**
     *
     * @param message
     */
    public void send(final String message) {
        try {
            SocketStreams.sendBytes(dos, message.getBytes());
        } catch (IOException ex) {
        }
    }

    /**
     *
     * @param group
     * @param message
     * @throws IOException
     */
    public void sendTo(final Group group, final byte[] message) throws IOException {
        for (SocketThread client : group.getClients()) {
            if (client.getChannel() == null) {
                client.send(message);
            }
        }
    }

    /**
     *
     * @param channel
     * @param message
     * @throws IOException
     */
    public void sendTo(final Channel channel, final byte[] message) throws IOException {
        for (SocketThread client : channel.getClients()) {
            client.send(message);
        }
    }

    /**
     *
     * @param group
     * @param message
     * @throws IOException
     */
    public void sendTo(final Group group, final String message) throws IOException {
        for (SocketThread client : group.getClients()) {
            if (client.getChannel() == null) {
                client.send(message);
            }
        }
    }

    /**
     *
     * @param channel
     * @param message
     * @throws IOException
     */
    public void sendTo(final Channel channel, final String message) throws IOException {
        for (SocketThread client : channel.getClients()) {
            client.send(message);
        }
    }

    /**
     *
     * @param message
     */
    public abstract void executeCommand(final String message);

    /**
     *
     */
    public void disconnect() {
        if (channel != null) {
            channel.kick(this);
        }

        if (group != null) {
            group.kick(this);
        }

        this.connected = false;
    }

    /**
     *
     * @throws IOException
     */
    private void close() throws IOException {
        socket.close();
    }

    /**
     *
     * @return
     */
    public Group getGroup() {
        return group;
    }

    /**
     *
     * @param group
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     *
     * @return
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     *
     * @param channel
     */
    public void setChannel(final Channel channel) {
        this.channel = channel;
    }

    /**
     *
     * @return
     */
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

}
