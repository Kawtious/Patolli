/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import patolli.game.online.server.threads.SocketThread;

public class Group extends Connection {

    private final List<Channel> channels = Collections.synchronizedList(new ArrayList<>());

    /**
     *
     * @param client
     * @param name
     */
    public Group(final SocketThread client, final String name) {
        id = UUID.randomUUID();
        this.name = name;
        password = "";

        client.setGroup(Group.this);

        add(client);
        operators.add(client);
    }

    /**
     *
     * @param client
     * @param name
     * @param password
     */
    public Group(final SocketThread client, final String name, final String password) {
        id = UUID.randomUUID();
        this.name = name;
        this.password = auth.hash(password.toCharArray());

        client.setGroup(Group.this);

        add(client);
        operators.add(client);
    }

    /**
     *
     */
    @Override
    public void destroy() {
        if (!clients.isEmpty()) {
            for (SocketThread client : clients) {
                kick(client);
            }
        }

        ServerManager.getInstance().getGroups().remove(this);
    }

    /**
     *
     * @param client
     */
    @Override
    public void kick(final SocketThread client) {
        client.setChannel(null);
        client.setGroup(null);
        clients.remove(client);

        if (clients.size() < 1) {
            destroy();
        }
    }

    /**
     *
     * @param client
     * @param name
     * @return
     */
    public Channel createChannel(final SocketThread client, final String name) {
        final Channel channel = new Channel(this, client, operators, name);
        channels.add(channel);
        return channel;
    }

    /**
     *
     * @param client
     * @param name
     * @param password
     * @return
     */
    public Channel createChannel(final SocketThread client, final String name, final String password) {
        final Channel channel = new Channel(this, client, operators, name, password);
        channels.add(channel);
        return channel;
    }

    /**
     *
     * @param channel
     */
    public void removeChannel(final Channel channel) {
        channel.destroy();
        channels.remove(channel);
    }

    /**
     *
     * @param channel
     * @param client
     */
    public void addClientToChannel(final Channel channel, final SocketThread client) {
        client.setChannel(channel);
        client.setGroup(null);
        channel.add(client);
    }

    /**
     *
     * @return
     */
    public List<Channel> getChannels() {
        return channels;
    }

}
