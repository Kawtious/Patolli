/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.Collections;
import java.util.List;
import patolli.game.online.client.PlayerSocket;

public class Playerlist {

    private final List<PlayerSocket> players;

    private int turn = 0;

    public Playerlist(final List<PlayerSocket> players) {
        this.players = players;
    }

    public void remove(final PlayerSocket player) {
        if (player.getPlayer().getBalance().isBroke()) {
            player.getPlayer().clearTokens();
        }

        if (player.equals(getCurrent())) {
            nextTurn();
        }

        players.remove(player);
        System.out.println(players.size());
    }

    public PlayerSocket getCurrent() {
        return players.get(turn);
    }

    public PlayerSocket getNext() {
        int index = turn + 1;

        if (index >= players.size()) {
            index = 0;
        }

        return players.get(index);
    }

    public PlayerSocket getPrev() {
        int index = turn - 1;

        if (index < 0) {
            index = players.size() - 1;
        }

        return players.get(index);
    }

    public void nextTurn() {
        turn++;

        if (turn >= players.size()) {
            turn = 0;
        }
    }

    public void prevTurn() {
        turn--;

        if (turn < 0) {
            turn = players.size() - 1;
        }
    }

    public int getTurn() {
        return turn;
    }

    public List<PlayerSocket> getPlayers() {
        return Collections.unmodifiableList(players);
    }

}
