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
import patolli.game.online.server.threads.SocketThread;

public class Channel extends Connection {

    private Pregame pregame;

    private Game game;

    private Group group;

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

        if (clients.size() < 1) {
            destroy();
        }
    }

    /**
     *
     */
    public void startGame() {
        if (game != null) {
            game = null;
        }

        System.out.println(pregame.getSettings().toString());

        pregame.getClients().addAll(clients);

        game = new Game(this);
        game.init();
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
