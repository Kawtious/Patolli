/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import patolli.game.online.PlayerSocket;

public class Leaderboard {

    private List<PlayerSocket> clients = Collections.synchronizedList(new ArrayList<>());

    private PlayerSocket winner = null;

    public Leaderboard(final List<PlayerSocket> clients) {
        this.clients = clients;
    }

    public void updateWinner() {
        for (PlayerSocket client : clients) {
            if (!client.getPlayer().getBalance().isBroke()) {
                if (winner == null) {
                    winner = client;
                } else {
                    if (winner.getPlayer().finishedTokens() < client.getPlayer().finishedTokens()) {
                        winner = client;
                    } else if (winner.getPlayer().finishedTokens() == client.getPlayer().finishedTokens()
                            && winner.getPlayer().getBalance().compare(client.getPlayer()) < 0) {
                        winner = client;
                    }
                }
            }
        }
    }

    public String printResults() {
        final StringBuilder sb = new StringBuilder();

        sb.append("Player ").append(winner.getPlayer().getName()).append(" wins the match!\n");

        for (PlayerSocket client : clients) {
            sb.append("Player ").append("Player ").append(client.getPlayer().getName()).append("'s tokens that finished: ").append(client.getPlayer().finishedTokens()).append("\n");
            sb.append("Player ").append(client.getPlayer().getName()).append("'s balance: ").append(client.getPlayer().getBalance()).append("\n");
        }

        return sb.toString();
    }
}
