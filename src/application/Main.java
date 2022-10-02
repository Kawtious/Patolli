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
        game.setBet(5);

        final ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("001", Color.yellow, 100));
        players.add(new Player("002", Color.red, 100));
        players.add(new Player("003", Color.green, 100));
        players.add(new Player("004", Color.blue, 100));

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
