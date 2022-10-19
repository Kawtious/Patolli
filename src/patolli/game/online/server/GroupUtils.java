/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import java.util.List;
import patolli.game.online.server.threads.IClientSocket;

public final class GroupUtils {

    /**
     *
     * @param list
     * @param client
     * @return
     */
    public static boolean isBanned(final List<IClientSocket> list, final IClientSocket client) {
        return list.contains(client);
    }

    /**
     *
     * @param list
     * @param client
     * @return
     */
    public static boolean isOperator(final List<IClientSocket> list, final IClientSocket client) {
        return list.contains(client);
    }

    private GroupUtils() {
    }

}
