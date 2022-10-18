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
import patolli.utils.Authentication;

public abstract class Connection {

    public final Authentication auth = new Authentication();

    public UUID id;

    public String name;

    public String password;

    public final List<SocketThread> clients = Collections.synchronizedList(new ArrayList<>());

    public final List<SocketThread> operators = Collections.synchronizedList(new ArrayList<>());

    public final List<SocketThread> blacklist = Collections.synchronizedList(new ArrayList<>());

    /**
     *
     * @return
     */
    public boolean hasPassword() {
        return !password.isEmpty();
    }

    /**
     *
     */
    public abstract void destroy();

    /**
     *
     * @param client
     */
    public void add(final SocketThread client) {
        clients.add(client);
    }

    /**
     *
     * @param client
     */
    public abstract void kick(final SocketThread client);

    /**
     *
     * @param client
     */
    public void ban(final SocketThread client) {
        kick(client);
        blacklist.add(client);
    }

    /**
     *
     * @param client
     */
    public void op(final SocketThread client) {
        operators.add(client);
    }

    /**
     *
     * @param client
     */
    public void deop(final SocketThread client) {
        operators.remove(client);
    }

    /**
     *
     * @return
     */
    public UUID getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    public void setPassword(final char[] password) {
        this.password = auth.hash(password);
    }

    /**
     * 
     * @return 
     */
    public List<SocketThread> getClients() {
        return clients;
    }

    /**
     * 
     * @return 
     */
    public List<SocketThread> getOperators() {
        return operators;
    }

    /**
     * 
     * @return 
     */
    public List<SocketThread> getBlacklist() {
        return blacklist;
    }

}
