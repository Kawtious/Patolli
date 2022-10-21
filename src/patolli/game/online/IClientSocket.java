/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;

public interface IClientSocket {

    /**
     *
     */
    public void start();

    /**
     *
     */
    public void run();

    /**
     *
     * @return
     */
    public byte[] listen();

    /**
     *
     * @param msg
     * @throws IOException
     */
    public void execute(final byte[] msg) throws IOException;

    /**
     *
     * @param message
     */
    public void executeCommand(final String message);

    /**
     *
     */
    public void disconnect();

    /**
     *
     * @return
     */
    public Socket getSocket();

    /**
     *
     * @return
     */
    public DataInputStream getDis();

    /**
     *
     * @return
     */
    public DataOutputStream getDos();

    /**
     *
     * @return
     */
    public byte[] getKey();

    /**
     *
     */
    public void setKey(byte[] key);

    /**
     *
     * @return
     */
    public Group getGroup();

    /**
     *
     * @param group
     */
    public void setGroup(Group group);

    /**
     *
     * @return
     */
    public Channel getChannel();

    /**
     *
     * @param channel
     */
    public void setChannel(final Channel channel);

    /**
     *
     * @param connected
     */
    public void setConnected(boolean connected);

}
