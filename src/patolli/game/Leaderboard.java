/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import utilities.console.Console;

public class Leaderboard {

    private ArrayList<Player> players;

    private Player winner = null;

    public Leaderboard(ArrayList<Player> players) {
        this.players = players;
    }

    public void updateWinner() {
        for (Player player : players) {
            if (!player.getBalance().isBroke()) {
                if (winner != null) {
                    if (winner.finishedTokens() < player.finishedTokens()) {
                        winner = player;
                    } else if (winner.finishedTokens() == player.finishedTokens()) {
                        if (winner.getBalance().compare(player) > 0) {
                            winner = player;
                        }
                    }
                } else {
                    winner = player;
                }
            }
        }
    }

    public void printResults() {
        Console.WriteLine("Leaderboard", "Player " + winner + " wins the match!");

        for (Player player : players) {
            Console.WriteLine("Leaderboard", "Player " + player + "'s tokens that finished: " + player.finishedTokens());
            Console.WriteLine("Leaderboard", "Player " + player + "'s balance: " + player.getBalance());
        }
    }
}
