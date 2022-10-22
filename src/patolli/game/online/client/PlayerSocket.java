/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.client;

import dradacorus.online.dragon.IDragonServer;
import dradacorus.online.kobold.KoboldSocket;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import patolli.game.Player;
import patolli.game.online.server.GameLayer;

public class PlayerSocket extends KoboldSocket {

    private Player player;

    private final PatolliCommands commands;

    public PlayerSocket(IDragonServer server, Socket socket) throws IOException {
        super(server, socket);
        this.player = new Player();
        this.commands = new PatolliCommands(server, this);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void executeCommand(List<String> arguments) {
        String execute = getArgument(arguments, 0);
        boolean modCommandExecuted = true;

        switch (execute) {
            // Player
            case "/setcolor" -> {
                commands.setColor(getArgument(arguments, 1));
            }

            // Settings
            case "/setsquares" -> {
                commands.setSquares(getArgument(arguments, 1));
            }
            case "/setbet" -> {
                commands.setBet(getArgument(arguments, 1));
            }
            case "/setmaxtokens" -> {
                commands.setMaxTokens(getArgument(arguments, 1));
            }
            case "/setbalance" -> {
                commands.setBalance(getArgument(arguments, 1));
            }
            case "/setmaxplayers" -> {
                commands.setMaxPlayers(getArgument(arguments, 1));
            }

            // Game
            case "/startgame" -> {
                commands.startGame();
            }
            case "/stopgame" -> {
                commands.stopGame();
            }
            case "/play" -> {
                commands.play(getArgument(arguments, 1));
            }

            //none of the mod commands were executed
            default -> {
                modCommandExecuted = false;
            }
        }

        if (!modCommandExecuted) {
            super.executeCommand(arguments);
        }
    }

    @Override
    public String getKoboldName() {
        return player.getName();
    }

    @Override
    public GameLayer getLair() {
        return (GameLayer) super.getLair();
    }

}
