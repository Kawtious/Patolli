/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.application;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import patolli.game.Game;
import patolli.game.Player;
import patolli.game.configuration.Balance;
import patolli.game.configuration.Pregame;
import patolli.game.configuration.Settings;
import patolli.game.tokens.Token;
import patolli.game.utils.Console;

public class Main {

    private static Player player = new Player("Kaw", Color.BLACK, new Balance());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final Settings settings = new Settings(4, 2, 2, 5, 3);
        final Pregame pregame = new Pregame(settings);

        final ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        players.add(new Player("Brad", Color.red, new Balance()));
        players.add(new Player("Caleb", Color.green, new Balance()));
        players.add(new Player("Dingus", Color.blue, new Balance()));
        pregame.add(players);

        pregame.shuffle();

        final Game game = new Game(pregame);

        if (!game.init()) {
            System.exit(1);
        }

        try (final Scanner scanner = new Scanner(System.in)) {

            while (!game.hasFinished()) {
                if (game.getCurrentPlayer() != player) {
                    game.play(null);
                } else {
                    if (player.tokensInPlay() > 1) {
                        game.play(selectToken(scanner));
                    } else {
                        game.play(null);
                    }
                }
            }

        }
    }

    public void shufflePlayers(final ArrayList<Player> players) {
        Console.WriteLine("Game", "Shuffling order of players");

        Collections.shuffle(players);
    }

    private static void printTokens() {
        for (int i = 0; i < player.countTokens(); i++) {
            Console.WriteLine("Main", "Token " + i + " at position " + player.getToken(i).getCurrentPos());
        }
    }

    private static Token selectToken(final Scanner scanner) {
        Console.WriteLine("Main", "It's your turn! Press enter to move token " + player.getCurrentToken().getIndex() + ", or type the index of the token you wish to move");
        printTokens();

        String readString = scanner.nextLine();

        while (readString != null) {
            if (readString.isEmpty()) {
                return null;
            }

            if (scanner.hasNextLine()) {
                readString = scanner.nextLine();

                if (validateTokenIndex(readString)) {
                    return player.getToken(Integer.parseInt(readString));
                }
            } else {
                readString = null;
            }
        }

        return null;
    }

    private static boolean validateTokenIndex(final String string) {
        if (string.isEmpty()) {
            return false;
        }

        char ch = string.charAt(0);

        if (Character.isDigit(ch)) {
            final int charValue = ch - '0';

            if (charValue >= 0 && charValue < player.countTokens()) {
                if (player.getToken(charValue).getCurrentPos() > 0) {
                    return true;
                }
            }
        }

        return false;
    }

}
