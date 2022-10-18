/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package patolli.game.online.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import patolli.game.Player;
import patolli.game.online.server.threads.SocketProtocol;
import patolli.game.online.server.threads.SocketThread;
import patolli.utils.Console;

public class Server {

    private final List<SocketThread> connections = Collections.synchronizedList(new ArrayList<>());

    private final List<Group> lobbies = Collections.synchronizedList(new ArrayList<>());

    public int port;

    private volatile boolean running = false;

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

                    add(new SocketProtocol(socket, new Player()));
                } catch (final IOException ex) {
                }
            }
        });
    }

    /**
     *
     * @param thread
     * @throws IOException
     */
    private void add(final SocketThread thread) throws IOException {
        connections.add(thread);
        thread.start();
    }

    /**
     *
     * @param group
     * @param client
     */
    public void addClientToGroup(final Group group, final SocketThread client) {
        client.setGroup(group);
        group.add(client);
    }

    /**
     *
     * @param client
     * @param name
     * @return
     */
    public Group createGroup(final SocketThread client, final String name) {
        final Group group = new Group(client, name);
        lobbies.add(group);
        return group;
    }

    /**
     *
     * @param client
     * @param name
     * @param password
     * @return
     */
    public Group createGroup(final SocketThread client, final String name, final String password) {
        final Group group = new Group(client, name, password);
        lobbies.add(group);
        return group;
    }

    /**
     *
     * @param group
     */
    public void removeGroup(final Group group) {
        group.destroy();
        lobbies.remove(group);
    }

    /**
     *
     * @return
     */
    public List<SocketThread> getConnections() {
        return connections;
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
    public List<Group> getGroups() {
        return lobbies;
    }

}
