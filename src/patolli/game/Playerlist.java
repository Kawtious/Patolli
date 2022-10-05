/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import patolli.game.utils.Console;

public class Playerlist {

    private Game game;

    private final ArrayList<Player> players = new ArrayList<>();

    private int turn = 0;

    public Playerlist(final Game game, final ArrayList<Player> players) {
        this.game = game;
        this.players.addAll(players);
    }

    public void add(final ArrayList<Player> players) {
        this.players.addAll(players);
    }

    public void remove(final Player player) {
        Console.WriteLine("Playerlist", "Removing player " + player + " from game");
        game.getBoard().removeTokensOf(player);
        players.remove(player);
    }

    public void update() {
        for (Player player : players) {
            if (player.getBalance().isBroke()) {
                remove(player);
            }
        }
    }

    public Player getCurrent() {
        return players.get(turn);
    }

    public Player get(final int index) {
        return players.get(index);
    }

    public int getTurnOf(final Player player) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).equals(player)) {
                return i;
            }
        }

        return -1;
    }

    public Player getNext() {
        int index = turn + 1;

        if (index >= players.size()) {
            index = 0;
        }

        return get(index);
    }

    public Player getPrev() {
        int index = turn - 1;

        if (index < 0) {
            index = players.size() - 1;
        }

        return get(index);
    }

    public void next() {
        turn++;

        if (turn >= players.size()) {
            turn = 0;
        }

        Console.WriteLine("Playerlist", "It is now player " + getCurrent() + "'s turn");
    }

    public void previous() {
        turn--;

        if (turn < 0) {
            turn = players.size() - 1;
        }
    }

    public Game getGame() {
        return game;
    }

    public int getTurn() {
        return turn;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

}
