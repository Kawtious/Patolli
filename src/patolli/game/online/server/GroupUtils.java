/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import java.util.List;
import patolli.game.online.IClientSocket;
import patolli.game.online.PlayerSocket;

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

    /**
     *
     * @param groups
     * @param name
     * @return
     */
    public static int findGroupByName(String name) {
        for (Group group1 : Server.getInstance().getGroups()) {
            if (group1.getName().equals(name)) {
                return Server.getInstance().getGroups().indexOf(group1);
            }
        }
        return -1;
    }

    /**
     *
     * @param channels
     * @param name
     * @return
     */
    public static int findChannelByName(Group group, String name) {
        for (Channel channel1 : group.getChannels()) {
            if (channel1.getName().equals(name)) {
                return group.getChannels().indexOf(channel1);
            }
        }
        return -1;
    }

    /**
     *
     * @param clients
     * @param name
     * @return
     */
    public static int findClientByName(List<IClientSocket> clients, String name) {
        for (IClientSocket client : clients) {
            PlayerSocket player1 = (PlayerSocket) client;
            if (player1.getPlayer().getName().equals(name)) {
                return clients.indexOf(player1);
            }
        }
        return -1;
    }

    private GroupUtils() {
    }

}
