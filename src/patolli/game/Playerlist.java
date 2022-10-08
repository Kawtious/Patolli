/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import patolli.game.online.server.threads.SocketThread;

public class Playerlist {

    private Game game;

    private final List<SocketThread> clients = Collections.synchronizedList(new ArrayList<>());

    private int turn = 0;

    public Playerlist(final Game game, final List<SocketThread> clients) {
        this.game = game;
        this.clients.addAll(clients);
    }

    public void add(final List<SocketThread> clients) {
        this.clients.addAll(clients);
    }

    public void remove(final SocketThread client, final boolean lost) {
        game.getBoard().removeTokensOf(client.getPlayer());
        clients.remove(client);

        if (lost) {
            client.getPlayer().clearTokens();
        }
    }

    public SocketThread getCurrent() {
        return clients.get(turn);
    }

    public SocketThread get(final int index) {
        return clients.get(index);
    }

    public int getTurnOf(final SocketThread client) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).equals(client)) {
                return i;
            }
        }

        return -1;
    }

    public SocketThread getNext() {
        int index = turn + 1;

        if (index >= clients.size()) {
            index = 0;
        }

        return get(index);
    }

    public SocketThread getPrev() {
        int index = turn - 1;

        if (index < 0) {
            index = clients.size() - 1;
        }

        return get(index);
    }

    public void next() {
        turn++;

        if (turn >= clients.size()) {
            turn = 0;
        }
    }

    public void previous() {
        turn--;

        if (turn < 0) {
            turn = clients.size() - 1;
        }
    }

    public Game getGame() {
        return game;
    }

    public int getTurn() {
        return turn;
    }

    public List<SocketThread> getClients() {
        return clients;
    }

}
