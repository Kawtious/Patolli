/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import patolli.game.online.server.threads.SocketThread;

public class Leaderboard {

    private List<SocketThread> clients = Collections.synchronizedList(new ArrayList<>());

    private SocketThread winner = null;

    public Leaderboard(final List<SocketThread> clients) {
        this.clients.addAll(clients);
    }

    public void updateWinner() {
        for (SocketThread client : clients) {
            if (!client.getPlayer().getBalance().isBroke()) {
                if (winner != null) {
                    if (winner.getPlayer().finishedTokens() < client.getPlayer().finishedTokens()) {
                        winner = client;
                    } else if (winner.getPlayer().finishedTokens() == client.getPlayer().finishedTokens()) {
                        if (winner.getPlayer().getBalance().compare(client.getPlayer()) > 0) {
                            winner = client;
                        }
                    }
                } else {
                    winner = client;
                }
            }
        }
    }

    public String printResults() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Player ").append(winner.getPlayer().getName()).append(" wins the match!\n");

        for (SocketThread client : clients) {
            sb.append("Player ").append("Player ").append(client.getPlayer().getName()).append("'s tokens that finished: ").append(client.getPlayer().finishedTokens()).append("\n");
            sb.append("Player ").append(client.getPlayer().getName()).append("'s balance: ").append(client.getPlayer().getBalance()).append("\n");
        }

        return sb.toString();
    }
}
