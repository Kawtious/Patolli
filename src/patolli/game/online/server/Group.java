/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import patolli.game.online.server.threads.IClientSocket;

public class Group implements IConnection {

    private UUID id = UUID.randomUUID();

    private String name = "";

    private String password = "";

    private final List<Channel> channels = Collections.synchronizedList(new ArrayList<>());

    /**
     *
     * @param client
     * @param name
     */
    public Group(final String name) {
        this.name = name;
    }

    /**
     *
     * @param client
     * @param name
     * @param password
     */
    public Group(final String name, final String password) {
        this.name = name;
        this.password = password;
    }

    /**
     *
     * @param client
     * @param name
     * @return
     */
    public Channel createChannel(final IClientSocket client, final String name) {
        final Channel channel = new Channel(this, name);
        channels.add(channel);

        client.setChannel(channel);
        client.setGroup(null);

        channel.add(client);
        channel.op(client);

        return channel;
    }

    /**
     *
     * @param client
     * @param name
     * @param password
     * @return
     */
    public Channel createChannel(final IClientSocket client, final String name, final String password) {
        final Channel channel = new Channel(this, name, password);
        channels.add(channel);

        client.setChannel(channel);
        client.setGroup(null);

        channel.add(client);
        channel.op(client);

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
    public void addClientToChannel(final Channel channel, final IClientSocket client) {
        client.setChannel(channel);
        client.setGroup(null);
        channel.add(client);
    }

    /**
     *
     * @return
     */
    public List<Channel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    private final List<IClientSocket> clients = Collections.synchronizedList(new ArrayList<>());

    private final List<IClientSocket> operators = Collections.synchronizedList(new ArrayList<>());

    private final List<IClientSocket> blacklist = Collections.synchronizedList(new ArrayList<>());

    /**
     *
     */
    @Override
    public void destroy() {
        if (!clients.isEmpty()) {
            for (IClientSocket client : clients) {
                kick(client);
            }
        }
    }

    /**
     *
     * @param client
     */
    @Override
    public void add(final IClientSocket client) {
        clients.add(client);
    }

    /**
     *
     * @param client
     */
    @Override
    public void kick(final IClientSocket client) {
        client.setChannel(null);
        client.setGroup(null);
        clients.remove(client);

        if (clients.size() < 1) {
            Server.getInstance().removeGroup(this);
        }
    }

    /**
     *
     * @param client
     */
    @Override
    public void ban(final IClientSocket client) {
        kick(client);
        blacklist.add(client);
    }

    /**
     *
     * @param client
     */
    @Override
    public void op(final IClientSocket client) {
        operators.add(client);
    }

    /**
     *
     * @param client
     */
    @Override
    public void deop(final IClientSocket client) {
        operators.remove(client);
    }

    /**
     *
     * @return
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasPassword() {
        return !password.isEmpty();
    }

    /**
     *
     * @return
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return
     */
    @Override
    public List<IClientSocket> getClients() {
        return Collections.unmodifiableList(clients);
    }

    /**
     *
     * @return
     */
    @Override
    public List<IClientSocket> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    /**
     *
     * @return
     */
    @Override
    public List<IClientSocket> getBlacklist() {
        return Collections.unmodifiableList(blacklist);
    }

}
