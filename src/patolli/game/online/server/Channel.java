/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import java.util.List;
import java.util.UUID;
import patolli.game.Game;
import patolli.game.configuration.Pregame;
import patolli.game.configuration.Pregame.Settings;
import patolli.game.online.server.threads.SocketStreams;
import patolli.game.online.server.threads.SocketThread;

public class Channel extends Connection {

    private final Group group;

    private final Pregame pregame;

    private Game game;

    /**
     *
     * @param group
     * @param client
     * @param operators
     * @param name
     */
    public Channel(final Group group, final SocketThread client, final List<SocketThread> operators, final String name) {
        pregame = new Pregame(new Settings());
        id = UUID.randomUUID();
        this.group = group;
        this.name = name;
        password = "";

        client.setChannel(Channel.this);
        client.setGroup(null);

        add(client);
        this.operators.addAll(operators);
    }

    /**
     *
     * @param group
     * @param client
     * @param operators
     * @param name
     * @param password
     */
    public Channel(final Group group, final SocketThread client, final List<SocketThread> operators, final String name, final String password) {
        pregame = new Pregame(new Settings());
        id = UUID.randomUUID();
        this.group = group;
        this.name = name;
        this.password = auth.hash(password.toCharArray());

        client.setChannel(Channel.this);
        client.setGroup(null);

        add(client);
        this.operators.addAll(operators);
    }

    /**
     *
     */
    @Override
    public void destroy() {
        if (!clients.isEmpty()) {
            for (SocketThread client : clients) {
                kick(client);
            }
        }

        group.getChannels().remove(this);
    }

    /**
     *
     * @param client
     */
    @Override
    public void kick(final SocketThread client) {
        client.setChannel(null);
        client.setGroup(group);
        clients.remove(client);

        if (game != null) {
            game.getPlayerlist().remove(client, true);
        }

        if (clients.size() < 1) {
            destroy();
        }
    }

    /**
     *
     */
    public void startGame() {
        if (game != null) {
            SocketStreams.sendTo(this, "A game is already running in this channel");
            return;
        }

        pregame.getClients().addAll(clients);

        game = new Game(this);

        if (!game.init()) {
            game = null;
        }
    }

    public void stopGame() {
        if (game == null) {
            SocketStreams.sendTo(this, "No game is running");
            return;
        }

        game = null;

        SocketStreams.sendTo(this, "Game has stopped");
    }

    /**
     *
     * @return
     */
    public Group getGroup() {
        return group;
    }

    /**
     *
     * @return
     */
    public Pregame getPregame() {
        return pregame;
    }

    /**
     *
     * @return
     */
    public Game getGame() {
        return game;
    }

}
