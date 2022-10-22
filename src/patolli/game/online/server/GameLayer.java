/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import dradacorus.online.client.IDragonSocket;
import dradacorus.online.server.IDragonServer;
import dradacorus.online.server.layers.Layer;
import dradacorus.online.utils.SocketHelper;
import java.util.ArrayList;
import java.util.List;
import patolli.game.Game;
import patolli.game.InvalidSettingsException;
import patolli.game.online.client.PlayerSocket;

public class GameLayer extends Layer {

    private Game game;

    public GameLayer(IDragonServer server, String name) {
        super(server, name);
    }

    public GameLayer(IDragonServer server, String name, String password) {
        super(server, name, password);
    }

    public void startGame() {
        if (game != null) {
            SocketHelper.sendTo(this, "A game is already running in this channel");
            return;
        }

        List<PlayerSocket> players = new ArrayList<>();

        game = new Game(this);

        for (int i = 0; i < game.getPreferences().getMaxPlayers() && i < getClients().size(); i++) {
            players.add((PlayerSocket) getClients().get(i));
        }

        for (PlayerSocket player : players) {
            player.getPlayer().getBalance().set(game.getPreferences().getInitBalance());
        }

        game.getSettings().add(players);

        try {
            game.getPreferences().validate();
        } catch (InvalidSettingsException ex) {
            SocketHelper.sendTo(this, ex.getMessage());
            return;
        }

        if (!game.init()) {
            game = null;
        }
    }

    public void stopGame() {
        if (game == null) {
            SocketHelper.sendTo(this, "No game is running");
            return;
        }

        game = null;
        SocketHelper.sendTo(this, "Game has stopped");
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void kick(IDragonSocket client) {
        if (game != null) {
            game.getPlayerlist().remove((PlayerSocket) client);
        }

        super.kick(client);
    }

}
