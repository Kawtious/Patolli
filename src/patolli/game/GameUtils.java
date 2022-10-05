/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.Random;
import utilities.console.Console;

public class GameUtils {

    public static int countCoins(final int amount) {
        Random random = new Random();
        int successes = 0;

        Console.WriteLine("GameUtils", "Throwing " + amount + " coin" + (amount == 1 ? "" : "s") + " for player");

        for (int i = 0; i < amount; i++) {
            if (random.nextBoolean()) {
                successes++;
            }
        }

        Console.WriteLine("GameUtils", successes + " coin" + (successes == 1 ? "" : "s") + " landed on true");

        return successes;
    }

    public static int calculateMovement(final int successes) {
        int result = 0;

        switch (successes) {
            case 1 -> {
                result = 1;
            }
            case 2 -> {
                result = 2;
            }
            case 3 -> {
                result = 3;
            }
            case 4 -> {
                result = 4;
            }
            case 5 -> {
                result = 10;
            }
        }

        Console.WriteLine("GameUtils", "Player will move " + result + " space" + (result == 1 ? "" : "s"));

        return result;
    }

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
