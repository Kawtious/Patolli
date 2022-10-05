/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.Collections;

public class Pregame {

    private final ArrayList<Player> players = new ArrayList<>();

    private Settings settings;

    public Pregame(Settings settings) {
        this.settings = settings;
    }

    public void add(final Player player) {
        players.add(player);
    }

    public void add(final ArrayList<Player> players) {
        this.players.addAll(players);
    }

    public void remove(final Player player) {
        players.remove(player);
    }

    public void shuffle() {
        Collections.shuffle(players);
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

}
