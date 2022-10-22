/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.client;

import dradacorus.online.client.DragonSocket;
import dradacorus.online.server.IDragonServer;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import patolli.game.Player;
import patolli.game.online.server.GameLayer;

public class PlayerSocket extends DragonSocket {

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

        switch (execute) {
            case "/help", "/?" -> {
                commands.help();
            }
            case "/setname", "/nickname", "/name" -> {
                commands.setClientName(getArgument(arguments, 1));
            }
            case "/createlayer" -> {
                commands.createLayer(getArgument(arguments, 1), getArgument(arguments, 2));
            }
            case "/joinlayer" -> {
                commands.joinLayer(getArgument(arguments, 1), getArgument(arguments, 2));
            }
            case "/leavelayer" -> {
                commands.leaveLayer();
            }
            case "/invite" -> {
                commands.invite(getArgument(arguments, 1), getArgument(arguments, 2));
            }
            case "/accept" -> {
                commands.accept(getArgument(arguments, 1));
            }
            case "/decline" -> {
                commands.decline(getArgument(arguments, 1));
            }
            case "/disconnect" -> {
                commands.disconnect();
            }
            case "/setlayername" -> {
                commands.setLayerName(getArgument(arguments, 1));
            }
            case "/setlayerpassword" -> {
                commands.setLayerPassword(getArgument(arguments, 1));
            }
            case "/kick" -> {
                commands.kick(getArgument(arguments, 1));
            }
            case "/ban" -> {
                commands.ban(getArgument(arguments, 1));
            }
            case "/op" -> {
                commands.op(getArgument(arguments, 1));
            }
            case "/deop" -> {
                commands.deop(getArgument(arguments, 1));
            }
            case "/listclients" -> {
                commands.listClients();
            }
            case "/listlayers" -> {
                commands.listLayers();
            }

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

            // Unknown
            default -> {
                commands.unknown(getArgument(arguments, 0));
            }
        }
    }

    @Override
    public String getClientName() {
        return player.getName();
    }

    @Override
    public void setClientName(String name) {
        player.setName(name);
    }

    @Override
    public GameLayer getLayer() {
        return (GameLayer) super.getLayer();
    }

}
