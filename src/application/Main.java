/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package application;

import entities.Player;
import java.awt.Color;
import java.util.ArrayList;

public class Main {

    private static final Game game = Game.getInstance();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //game();
        game.setSquares(2);
        game.setBet(5);
        game.setMaxTokens(2);

        final ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Al", Color.yellow, 100, game.getMaxTokens()));
        players.add(new Player("Brad", Color.red, 100, game.getMaxTokens()));
        players.add(new Player("Caleb", Color.green, 100, game.getMaxTokens()));
//        players.add(new Player("Dingus", Color.blue, 100, game.getMaxTokens()));

        game.addPlayers(players);
        //game.shufflePlayers();

        if (!game.run()) {
            System.exit(1);
        }

        // Player 1 plays
        while (!game.hasFinished()) {
            game.play(null);
        }

    }

}
