/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import patolli.game.Player;
import patolli.game.online.server.threads.SocketThread;
import patolli.utils.Console;

public class Pregame {

    private final List<SocketThread> clients = Collections.synchronizedList(new ArrayList<>());

    private Settings settings;

    public Pregame(Settings settings) {
        this.settings = settings;
    }

    public void add(final SocketThread client) {
        clients.add(client);
    }

    public void add(final List<SocketThread> clients) {
        this.clients.addAll(clients);
    }

    public void remove(final SocketThread client) {
        clients.remove(client);
    }

    public void shuffle() {
        Collections.shuffle(clients);
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public List<SocketThread> getClients() {
        return clients;
    }

    public ArrayList<Player> getPlayers() {
        final ArrayList<Player> players = new ArrayList<>();
        for (SocketThread client : clients) {
            players.add(client.getPlayer());
        }

        return players;
    }

    public static class Settings {

        private int maxPlayers;

        private int squares;

        private int triangles;

        private int bet;

        private int maxTokens;

        private int initBalance;

        private final int DEFAULT_MAXPLAYERS = 4,
                DEFAULT_SQUARES = 3,
                DEFAULT_TRIANGLES = 2,
                DEFAULT_BET = 5,
                DEFAULT_MAXTOKENS = 3,
                DEFAULT_INITBALANCE = 100;

        public Settings() {
            this.maxPlayers = DEFAULT_MAXPLAYERS;
            this.squares = DEFAULT_SQUARES;
            this.triangles = DEFAULT_TRIANGLES;
            this.bet = DEFAULT_BET;
            this.maxTokens = DEFAULT_MAXTOKENS;
            this.initBalance = DEFAULT_INITBALANCE;
        }

        public Settings(int maxPlayers, int squares, int triangles, int bet, int maxTokens, int initBalance) {
            this.maxPlayers = maxPlayers;
            this.squares = squares;
            this.triangles = triangles;
            this.bet = bet;
            this.maxTokens = maxTokens;
            this.initBalance = DEFAULT_INITBALANCE;
        }

        public boolean validate() {
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
            sb.append(", triangles=").append(triangles);
            sb.append(", bet=").append(bet);
            sb.append(", maxTokens=").append(maxTokens);
            sb.append('}');
            return sb.toString();
        }

    }

}
