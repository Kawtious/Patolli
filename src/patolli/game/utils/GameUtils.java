/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.utils;

import java.util.ArrayList;
import patolli.game.Player;

public class GameUtils {

    public static void pay(final int amount, final Player from, final Player to) {
        Console.WriteLine("GameUtils", "Player " + from + " pays " + amount + " to player " + to.getName());

        from.getBalance().take(amount);
        to.getBalance().give(amount);

        Console.WriteLine("GameUtils", "Player " + from + ":  " + from.getBalance() + " | Player " + to.getName() + ": " + to.getBalance());
    }

    public static void payEveryone(final int amount, final Player from, final ArrayList<Player> to) {
        Console.WriteLine("GameUtils", "Player " + from + " pays " + amount + " to everyone");

        for (Player player : to) {
            if (!player.equals(from)) {
                pay(amount, from, player);
            }
        }
    }

    public static void everyonePays(final int amount, final ArrayList<Player> from, final Player to) {
        Console.WriteLine("GameUtils", "Everyone pays " + amount + " to " + to);

        for (Player player : from) {
            if (!player.equals(to)) {
                pay(amount, player, to);
            }
        }
    }

}
