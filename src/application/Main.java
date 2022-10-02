/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package application;

import entities.Player;
import entities.Token;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Scanner;
import utilities.Console;

public class Main {

    private static final Game game = Game.getInstance();

    private static Player player;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        game.setSquares(2);
        game.setBet(5);
        game.setMaxTokens(3);

        player = new Player("Kaw", Color.BLACK, 100, game.getMaxTokens());

        final ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        players.add(new Player("Brad", Color.red, 100, game.getMaxTokens()));
        players.add(new Player("Caleb", Color.green, 100, game.getMaxTokens()));
        players.add(new Player("Dingus", Color.blue, 100, game.getMaxTokens()));

        game.addPlayers(players);

        //game.shufflePlayers();
        if (!game.run()) {
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

    private static void printTokens() {
        for (int i = 1; i <= player.getTokensCount(); i++) {
            Console.WriteLine("Token " + (i) + " at position " + player.getToken(i).getCurrentPos());
        }
    }

    private static Token selectToken(final Scanner scanner) {
        Console.WriteLine("It's your turn! Press enter to move next token, or type the index of the token you wish to move");
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

            if (charValue > 0 && charValue < player.getTokensCount()) {
                if (player.getToken(charValue).getCurrentPos() > 0) {
                    return true;
                }
            }
        }

        return false;
    }

}
