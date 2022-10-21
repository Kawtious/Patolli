/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import patolli.game.online.PlayerSocket;
import patolli.utils.Console;

public class Settings {

    private final List<PlayerSocket> players = Collections.synchronizedList(new ArrayList<>());

    private Preferences preferences;

    public Settings(Preferences preferences) {
        this.preferences = preferences;
    }

    public void add(final PlayerSocket player) {
        players.add(player);
    }

    public void add(final List<PlayerSocket> players) {
        this.players.addAll(players);
    }

    public void remove(final PlayerSocket player) {
        players.remove(player);
    }

    public void shuffle() {
        Collections.shuffle(players);
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public List<PlayerSocket> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public static class Preferences {

        private int maxPlayers;

        private int squares;

        private int bet;

        private int maxTokens;

        private int initBalance;

        private final int DEFAULT_MAXPLAYERS = 4,
                DEFAULT_SQUARES = 3,
                DEFAULT_BET = 5,
                DEFAULT_MAXTOKENS = 3,
                DEFAULT_INITBALANCE = 100;

        public Preferences() {
            this.maxPlayers = DEFAULT_MAXPLAYERS;
            this.squares = DEFAULT_SQUARES;
            this.bet = DEFAULT_BET;
            this.maxTokens = DEFAULT_MAXTOKENS;
            this.initBalance = DEFAULT_INITBALANCE;
        }

        public Preferences(int maxPlayers, int squares, int triangles, int bet, int maxTokens, int initBalance) {
            this.maxPlayers = maxPlayers;
            this.squares = squares;
            this.bet = bet;
            this.maxTokens = maxTokens;
            this.initBalance = initBalance;
        }

        public boolean validate() {
            if (maxPlayers < 2) {
                Console.WriteLine("Settings", "Not enough players");
                return false;
            }

            if (maxPlayers > 4) {
                Console.WriteLine("Settings", "Too many players");
                return false;
            }

            if (bet < 5) {
                Console.WriteLine("Settings", "Bet has to be greater than 5 in order to play");
                return false;
            }

            if (bet > initBalance / 3) {
                Console.WriteLine("Settings", "Bet too big");
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

        public int getInitBalance() {
            return initBalance;
        }

        public void setInitBalance(int initBalance) {
            this.initBalance = initBalance;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Settings{");
            sb.append("maxPlayers=").append(maxPlayers);
            sb.append(", squares=").append(squares);
            sb.append(", bet=").append(bet);
            sb.append(", maxTokens=").append(maxTokens);
            sb.append('}');
            return sb.toString();
        }

    }

}
