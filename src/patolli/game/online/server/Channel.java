/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import patolli.game.Game;
import patolli.game.configuration.Settings;
import patolli.game.configuration.Settings.Preferences;
import patolli.game.online.server.threads.IClientSocket;
import patolli.game.online.server.threads.PlayerSocket;
import patolli.utils.SocketHelper;

public class Channel implements IConnection {

    private UUID id = UUID.randomUUID();

    private String name = "";

    private String password = "";

    private final Group group;

    private Settings settings = new Settings(new Preferences());

    private Game game;

    /**
     *
     * @param group
     * @param client
     * @param operators
     * @param name
     */
    public Channel(final Group group, final String name) {
        this.group = group;
        this.name = name;
    }

    /**
     *
     * @param group
     * @param client
     * @param operators
     * @param name
     * @param password
     */
    public Channel(final Group group, final String name, final String password) {
        this.group = group;
        this.name = name;
        this.password = password;
    }

    /**
     *
     */
    public void startGame() {
        if (game != null) {
            SocketHelper.sendTo(this, "A game is already running in this channel");
            return;
        }

        List<PlayerSocket> players = new ArrayList<>();

        for (int i = 0; i < settings.getPreferences().getMaxPlayers() && i < clients.size(); i++) {
            players.add((PlayerSocket) clients.get(i));
        }

        for (PlayerSocket client : players) {
            client.getPlayer().getBalance().set(settings.getPreferences().getInitBalance());
        }

        settings.add(players);

        game = new Game(this);

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
        settings = new Settings(new Preferences());
        SocketHelper.sendTo(this, "Game has stopped");
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
    public Settings getSettings() {
        return settings;
    }

    /**
     *
     * @return
     */
    public Game getGame() {
        return game;
    }

    private final List<IClientSocket> clients = Collections.synchronizedList(new ArrayList<>());

    private final List<IClientSocket> operators = Collections.synchronizedList(new ArrayList<>());

    private final List<IClientSocket> blacklist = Collections.synchronizedList(new ArrayList<>());

    /**
     *
     */
    @Override
    public void destroy() {
        if (!clients.isEmpty()) {
            for (IClientSocket client : clients) {
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
    public void add(final IClientSocket client) {
        clients.add(client);
    }

    /**
     *
     * @param client
     */
    @Override
    public void kick(final IClientSocket client) {
        client.setChannel(null);
        client.setGroup(group);
        clients.remove(client);

        if (game != null) {
            game.getPlayerlist().remove((PlayerSocket) client);
        }

        if (clients.size() < 1) {
            destroy();
        }
    }

    /**
     *
     * @param client
     */
    @Override
    public void ban(final IClientSocket client) {
        kick(client);
        blacklist.add(client);
    }

    /**
     *
     * @param client
     */
    @Override
    public void op(final IClientSocket client) {
        operators.add(client);
    }

    /**
     *
     * @param client
     */
    @Override
    public void deop(final IClientSocket client) {
        operators.remove(client);
    }

    /**
     *
     * @return
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    @Override
    public void setName(final String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasPassword() {
        return !password.isEmpty();
    }

    /**
     *
     * @return
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return
     */
    @Override
    public List<IClientSocket> getClients() {
        return Collections.unmodifiableList(clients);
    }

    /**
     *
     * @return
     */
    @Override
    public List<IClientSocket> getOperators() {
        return Collections.unmodifiableList(operators);
    }

    /**
     *
     * @return
     */
    @Override
    public List<IClientSocket> getBlacklist() {
        return Collections.unmodifiableList(blacklist);
    }

}
