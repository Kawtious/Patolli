/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import java.util.List;
import java.util.UUID;
import patolli.game.online.IClientSocket;

public interface IConnection {

    /**
     *
     */
    public void destroy();

    /**
     *
     * @param client
     */
    public void add(IClientSocket client);

    /**
     *
     * @param client
     */
    public void kick(IClientSocket client);

    /**
     *
     * @param client
     */
    public void ban(IClientSocket client);

    /**
     *
     * @param client
     */
    public void op(IClientSocket client);

    /**
     *
     * @param client
     */
    public void deop(IClientSocket client);

    /**
     *
     * @return
     */
    public UUID getId();

    /**
     *
     * @param id
     */
    public void setId(final UUID id);

    /**
     *
     * @return
     */
    public String getName();

    /**
     *
     * @param name
     */
    public void setName(final String name);

    /**
     *
     * @return
     */
    public boolean hasPassword();

    /**
     *
     * @return
     */
    public String getPassword();

    /**
     *
     * @param password
     */
    public void setPassword(final String password);

    /**
     *
     * @return
     */
    public List<IClientSocket> getClients();

    /**
     *
     * @return
     */
    public List<IClientSocket> getOperators();

    /**
     *
     * @return
     */
    public List<IClientSocket> getBlacklist();

}
