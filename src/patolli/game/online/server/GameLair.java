/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import dradacorus.online.ExtendableLair;
import dradacorus.online.IDragonServer;
import dradacorus.online.IKoboldSocket;
import dradacorus.online.utils.SocketHelper;
import java.util.ArrayList;
import java.util.List;
import patolli.game.Game;
import patolli.game.InvalidSettingsException;
import patolli.game.online.client.PlayerSocket;

public class GameLair extends ExtendableLair {

    private Game game;

    public GameLair(IDragonServer dragon, String name) {
        super(dragon, name);
    }

    public GameLair(IDragonServer dragon, String name, String password) {
        super(dragon, name, password);
    }

    public void startGame() {
        if (game != null) {
            SocketHelper.Output.sendTo(this, "A game is already running in this lair");
            return;
        }

        List<PlayerSocket> players = new ArrayList<>();

        game = new Game(this);

        for (int i = 0; i < game.getPreferences().getMaxPlayers() && i < getKobolds().size(); i++) {
            players.add((PlayerSocket) getKobolds().get(i));
        }

        for (PlayerSocket player : players) {
            player.getPlayer().getBalance().set(game.getPreferences().getInitBalance());
        }

        game.getSettings().add(players);

        try {
            game.getPreferences().validate();
        } catch (InvalidSettingsException ex) {
            SocketHelper.Output.sendTo(this, ex.getMessage());
            return;
        }

        if (!game.init()) {
            game = null;
        }
    }

    public void stopGame() {
        if (game == null) {
            SocketHelper.Output.sendTo(this, "No game is running");
            return;
        }

        game = null;
        SocketHelper.Output.sendTo(this, "Game has stopped");
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void kick(IKoboldSocket client) {
        if (game != null) {
            game.getPlayerlist().remove((PlayerSocket) client);
        }

        super.kick(client);
    }

}
