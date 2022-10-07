/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online;

import java.util.List;
import patolli.game.online.server.threads.SocketThread;

public final class ClientUtils {

    /**
     *
     * @param list
     * @param client
     * @return
     */
    public static boolean isBanned(final List<SocketThread> list, final SocketThread client) {
        return list.contains(client);
    }

    /**
     *
     * @param list
     * @param client
     * @return
     */
    public static boolean isOperator(final List<SocketThread> list, final SocketThread client) {
        return list.contains(client);
    }

}
