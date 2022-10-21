/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.List;
import patolli.game.online.PlayerSocket;

public class Leaderboard {

    private List<PlayerSocket> players = new ArrayList<>();

    private PlayerSocket winner = null;

    public Leaderboard(List<PlayerSocket> players) {
        this.players = players;
    }

    public void updateWinner() {
        for (PlayerSocket player : players) {
            if (!player.getPlayer().getBalance().isBroke()) {
                if (winner == null) {
                    winner = player;
                } else {
                    if (winner.getPlayer().finishedTokens() < player.getPlayer().finishedTokens()) {
                        winner = player;
                    } else if (winner.getPlayer().finishedTokens() == player.getPlayer().finishedTokens()
                            && winner.getPlayer().getBalance().compare(player.getPlayer()) < 0) {
                        winner = player;
                    }
                }
            }
        }
    }

    public String printResults() {
        final StringBuilder sb = new StringBuilder();

        sb.append("Player ").append(winner.getPlayer().getName()).append(" wins the match!\n");
        for (PlayerSocket player : players) {
            sb.append("Player ").append("Player ").append(player.getPlayer().getName()).append("'s tokens that finished: ").append(player.getPlayer().finishedTokens()).append("\n");
            sb.append("Player ").append(player.getPlayer().getName()).append("'s balance: ").append(player.getPlayer().getBalance()).append("\n");
        }

        return sb.toString();
    }
}
