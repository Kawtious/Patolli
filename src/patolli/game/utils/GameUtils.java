/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.utils;

import java.util.List;
import patolli.game.Game;
import patolli.game.online.server.threads.SocketStreams;
import patolli.game.online.server.threads.SocketThread;

public class GameUtils {

    public static void pay(final Game game, final int amount, final SocketThread from, final SocketThread to) {
        SocketStreams.sendTo(game.getChannel(), "Player " + from.getPlayer().getName() + " pays " + amount + " to player " + to.getPlayer().getName());

        from.getPlayer().getBalance().take(amount);
        to.getPlayer().getBalance().give(amount);

        SocketStreams.sendTo(game.getChannel(), "Player " + from.getPlayer().getName() + ":  " + from.getPlayer().getBalance() + " | Player " + to.getPlayer().getName() + ": " + to.getPlayer().getBalance());
    }

    public static void payEveryone(final Game game, final int amount, final SocketThread from, final List<SocketThread> to) {
        SocketStreams.sendTo(game.getChannel(), "Player " + from.getPlayer().getName() + " pays " + amount + " to everyone");

        for (SocketThread player : to) {
            if (!player.equals(from)) {
                pay(game, amount, from, player);
            }
        }
    }

    public static void everyonePays(final Game game, final int amount, final List<SocketThread> from, final SocketThread to) {
        SocketStreams.sendTo(game.getChannel(), "Everyone pays " + amount + " to " + to.getPlayer().getName());

        for (SocketThread player : from) {
            if (!player.equals(to)) {
                pay(game, amount, player, to);
            }
        }
    }

}
