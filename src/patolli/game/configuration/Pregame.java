/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.configuration;

import java.util.ArrayList;
import java.util.Collections;
import patolli.game.Player;
import patolli.game.utils.Console;

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

    public static class Settings {

        private int maxPlayers;

        private int squares;

        private int triangles;

        private int bet;

        private int maxTokens;

        private final int DEFAULT_MAXPLAYERS = 4,
                DEFAULT_SQUARES = 3,
                DEFAULT_TRIANGLES = 2,
                DEFAULT_BET = 5,
                DEFAULT_MAXTOKENS = 3;

        public Settings() {
            this.maxPlayers = DEFAULT_MAXPLAYERS;
            this.squares = DEFAULT_SQUARES;
            this.triangles = DEFAULT_TRIANGLES;
            this.bet = DEFAULT_BET;
            this.maxTokens = DEFAULT_MAXTOKENS;
        }

        public Settings(int maxPlayers, int squares, int triangles, int bet, int maxTokens) {
            this.maxPlayers = maxPlayers;
            this.squares = squares;
            this.triangles = triangles;
            this.bet = bet;
            this.maxTokens = maxTokens;
        }

        public boolean validate() {
            if (bet < 5) {
                Console.WriteLine("Settings", "Bet has to be greater than 5 in order to play");
                return false;
            }

            if (maxTokens < 2) {
                Console.WriteLine("Settings", "Each player must have a minimum of 2 tokens to play");
                return false;
            }

            if (squares < 2) {
                Console.WriteLine("Settings", "Board must have a minimum of 2 common spaces per side of each blade");
                return false;
            }

            return true;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        public int getSquares() {
            return squares;
        }

        public void setSquares(int squares) {
            this.squares = squares;
        }

        public int getTriangles() {
            return triangles;
        }

        public void setTriangles(int triangles) {
            this.triangles = triangles;
        }

        public int getBet() {
            return bet;
        }

        public void setBet(int bet) {
            this.bet = bet;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

    }

}
