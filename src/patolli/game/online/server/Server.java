/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.game.online.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import patolli.game.Player;
import patolli.game.online.IClientSocket;
import patolli.game.online.PlayerSocket;
import patolli.utils.Console;
import patolli.utils.SocketHelper;

public class Server {

    private int port;

    private volatile boolean running = false;

    private final List<IClientSocket> connections = Collections.synchronizedList(new ArrayList<>());

    private final List<Group> groups = Collections.synchronizedList(new ArrayList<>());

    private static Server instance;

    /**
     * Singleton pattern to keep a single instance of this class program running
     *
     * @return The instance of the program is returned, if there's none a new one is created
     */
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }

        return instance;
    }

    /**
     *
     */
    private Server() {
    }

    /**
     *
     * @return
     */
    public boolean start() {
        if (port < 0 || port > 65535) {
            return false;
        }

        run(port);

        return true;
    }

    /**
     *
     * @param port
     */
    private void run(final int port) {
        try (final ServerSocket server = new ServerSocket(port)) {
            running = true;

            listen(server).start();

            Console.WriteLine("Server", "Server is now running!");
            Console.WriteLine("Server", "Listening on port " + port);

            while (running) {
            }
        } catch (final IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     *
     * @param server
     * @return
     */
    private Thread listen(final ServerSocket server) {
        return new Thread(() -> {
            while (running) {
                try {
                    Socket socket = server.accept();
                    IClientSocket client = new PlayerSocket(socket, new Player());

                    byte[] key = UUID.randomUUID().toString().getBytes();

                    if (validate(client, key)) {
                        add(client);
                    }
                } catch (final IOException ex) {
                }
            }
        });
    }

    /**
     *
     */
    private boolean validate(IClientSocket client, byte[] key) throws IOException {
        SocketHelper.send(client, key);
        client.setKey(key);

        if (Arrays.equals(client.listen(), client.getKey())) {
            Console.WriteLine("Server", client.getSocket().getInetAddress() + " has connected");
            SocketHelper.send(client, "Type /? or /help for a list of commands");
            return true;
        }

        return false;
    }

    /**
     *
     * @param thread
     * @throws IOException
     */
    private void add(final IClientSocket thread) throws IOException {
        connections.add(thread);
        thread.setConnected(true);
        thread.start();
    }

    /**
     *
     * @param group
     * @param client
     */
    public void addClientToGroup(final Group group, final IClientSocket client) {
        client.setGroup(group);
        group.add(client);
    }

    /**
     *
     * @param client
     * @param name
     * @return
     */
    public Group createGroup(final IClientSocket client, final String name) {
        final Group group = new Group(name);
        groups.add(group);

        client.setGroup(group);

        group.add(client);
        group.op(client);

        return group;
    }

    /**
     *
     * @param client
     * @param name
     * @param password
     * @return
     */
    public Group createGroup(final IClientSocket client, final String name, final String password) {
        final Group group = new Group(name, password);
        groups.add(group);

        client.setGroup(group);

        group.add(client);
        group.op(client);

        return group;
    }

    /**
     *
     * @param group
     */
    public void removeGroup(final Group group) {
        group.destroy();
        groups.remove(group);
    }

    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    /**
     *
     * @return
     */
    public List<IClientSocket> getConnections() {
        return Collections.unmodifiableList(connections);
    }

    /**
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

}
