/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.server.threads;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import patolli.game.Player;
import patolli.game.Token;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;
import patolli.game.online.server.GroupUtils;
import patolli.game.online.server.Server;
import patolli.utils.Console;
import patolli.utils.ValidationUtils;

public class PlayerSocket extends Thread implements IClientSocket {

    private byte[] key = "$31$".getBytes();

    private final Socket socket;

    private final DataInputStream dis;

    private final DataOutputStream dos;

    private Group group;

    private Channel channel;

    private volatile boolean connected = false;

    private Player player;

    private final Commands commands = new Commands();

    /**
     *
     * @param socket
     * @param player
     * @throws IOException
     */
    public PlayerSocket(Socket socket, Player player) throws IOException {
        this.socket = socket;
        this.player = player;
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
    }

    /**
     *
     * @param syntaxes
     * @param index
     * @return
     */
    private String getSyntax(final String[] syntaxes, final int index) {
        if (index < 0) {
            return "";
        }

        if (syntaxes.length == 0) {
            return "";
        }

        if (index >= syntaxes.length) {
            return "";
        }

        return syntaxes[index];
    }

    /**
     *
     * @return
     */
    public Player getPlayer() {
        return player;
    }

    /**
     *
     * @param player
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     *
     */
    @Override
    public void run() {
        try (socket; dos; dis) {
            while (connected) {
                listen();
            }

            Console.WriteLine("PlayerSocket", player.getName() + " disconnected from server");
        } catch (IOException ex) {
            Logger.getLogger(IClientSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public byte[] listen() {
        try {
            byte[] input = SocketStreams.readBytes(dis, key);

            if (connected) {
                execute(input);
                //Console.WriteLine("PlayerSocket", getPlayer().getName() + ": " + new String(TinkHelper.encryptBytes(input, key), StandardCharsets.US_ASCII));
                Console.WriteLine("PlayerSocket", getPlayer().getName() + ": " + new String(input));
            }

            return input;
        } catch (final IOException ex) {
            disconnect();
        }

        return new byte[1];
    }

    /**
     *
     * @param msg
     * @throws IOException
     */
    @Override
    public void execute(final byte[] msg) throws IOException {
        final String input = new String(msg);

        if (ValidationUtils.validateCommand(input)) {
            executeCommand(input);
        } else {
            if (getChannel() != null) {
                SocketStreams.sendTo(getChannel(), getPlayer().getName() + ": " + input);
            } else if (getGroup() != null) {
                // send messages
                SocketStreams.sendTo(getGroup(), getPlayer().getName() + ": " + input);
            }
        }
    }

    /**
     *
     * @param message
     */
    @Override
    public void executeCommand(final String message) {
        final String[] syntaxes = message.split("\\s+");
        final String execute = syntaxes[0];

        switch (execute) {
            case "/help", "/?" -> {
                commands.help();
            }
            case "/leavegroup", "/leavelobby" -> {
                commands.leaveGroup();
            }
            case "/leavechannel", "/leaveroom" -> {
                commands.leaveChannel();
            }
            case "/joinchannel", "/joinroom" -> {
                commands.joinChannel(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/disconnect" -> {
                commands.disconnect();
            }
            case "/joingroup", "/joinlobby" -> {
                commands.joinGroup(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/creategroup", "/createlobby" -> {
                commands.createGroup(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/createchannel", "/createroom" -> {
                commands.createChannel(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/setgroupname", "/setlobbyname" -> {
                commands.setGroupName(getSyntax(syntaxes, 1));
            }
            case "/setchannelname", "/setroomname" -> {
                commands.setChannelName(getSyntax(syntaxes, 1));
            }
            case "/setgrouppassword", "/setlobbypassword" -> {
                commands.setGroupPassword(getSyntax(syntaxes, 1));
            }
            case "/setchannelpassword", "/setroompassword" -> {
                commands.setChannelPassword(getSyntax(syntaxes, 1));
            }
            case "/kick" -> {
                commands.kick(getSyntax(syntaxes, 1));
            }
            case "/ban" -> {
                commands.ban(getSyntax(syntaxes, 1));
            }
            case "/op" -> {
                commands.op(getSyntax(syntaxes, 1));
            }
            case "/deop" -> {
                commands.deop(getSyntax(syntaxes, 1));
            }
            case "/list" -> {
                commands.list();
            }

            // Player
            case "/setname", "/nickname", "/name" -> {
                commands.setPlayerName(getSyntax(syntaxes, 1));
            }
            case "/setcolor" -> {
                commands.setColor(getSyntax(syntaxes, 1));
            }

            // Settings
            case "/startgame" -> {
                commands.startGame();
            }
            case "/stopgame" -> {
                commands.stopGame();
            }
            case "/setsquares" -> {
                commands.setSquares(getSyntax(syntaxes, 1));
            }
            case "/setbet" -> {
                commands.setBet(getSyntax(syntaxes, 1));
            }
            case "/setmaxtokens" -> {
                commands.setMaxTokens(getSyntax(syntaxes, 1));
            }
            case "/setbalance" -> {
                commands.setBalance(getSyntax(syntaxes, 1));
            }

            // Game
            case "/play" -> {
                commands.play(getSyntax(syntaxes, 1));
            }

            // Unknown
            default -> {
                commands.unknown(getSyntax(syntaxes, 0));
            }
        }
    }

    /**
     *
     */
    @Override
    public void disconnect() {
        if (channel != null) {
            channel.kick(this);
        }

        if (group != null) {
            group.kick(this);
        }

        this.connected = false;
    }

    /**
     *
     * @return
     */
    @Override
    public Group getGroup() {
        return group;
    }

    /**
     *
     * @param group
     */
    @Override
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     *
     * @return
     */
    @Override
    public Channel getChannel() {
        return channel;
    }

    /**
     *
     * @param channel
     */
    @Override
    public void setChannel(final Channel channel) {
        this.channel = channel;
    }

    /**
     *
     * @return
     */
    @Override
    public byte[] getKey() {
        return key != null ? Arrays.copyOf(key, key.length) : null;
    }

    /**
     *
     * @param key
     */
    @Override
    public void setKey(byte[] key) {
        this.key = Arrays.copyOf(key, key.length);
    }

    /**
     *
     * @param connected
     */
    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public DataInputStream getDis() {
        return dis;
    }

    @Override
    public DataOutputStream getDos() {
        return dos;
    }

    public class Commands {

        /**
         *
         */
        public Commands() {
        }

        /**
         *
         */
        public void help() {
            final StringBuilder sb = new StringBuilder();

            sb.append("List of commands:");

            sb.append("\n");

            sb.append("/help");
            sb.append(", ");
            sb.append("/leavegroup");
            sb.append(", ");
            sb.append("/leavelobby");
            sb.append(", ");
            sb.append("/leavechannel");
            sb.append(", ");
            sb.append("/leaveroom");
            sb.append(", ");
            sb.append("/joinchannel");
            sb.append(", ");
            sb.append("/joinroom");
            sb.append(", ");
            sb.append("/disconnect");
            sb.append(", ");
            sb.append("/joingroup");
            sb.append(", ");
            sb.append("/joinlobby");
            sb.append(", ");
            sb.append("/creategroup");
            sb.append(", ");
            sb.append("/createlobby");
            sb.append(", ");
            sb.append("/createchannel");
            sb.append(", ");
            sb.append("/createroom");
            sb.append(", ");
            sb.append("/setgroupname");
            sb.append(", ");
            sb.append("/setlobbyname");
            sb.append(", ");
            sb.append("/setchannelname");
            sb.append(", ");
            sb.append("/setroomname");
            sb.append(", ");
            sb.append("/setgrouppassword");
            sb.append(", ");
            sb.append("/setlobbypassword");
            sb.append(", ");
            sb.append("/setchannelpassword");
            sb.append(", ");
            sb.append("/setroompassword");
            sb.append(", ");
            sb.append("/kick");
            sb.append(", ");
            sb.append("/ban");
            sb.append(", ");
            sb.append("/op");
            sb.append(", ");
            sb.append("/deop");
            sb.append(", ");
            sb.append("/list");

            sb.append("\n");

            // Player
            sb.append("/setname");
            sb.append(", ");
            sb.append("/nickname");
            sb.append(", ");
            sb.append("/name");
            sb.append(", ");
            sb.append("/setcolor");

            sb.append("\n");

            // Settings
            sb.append("/startgame");
            sb.append(", ");
            sb.append("/stopgame");
            sb.append(", ");
            sb.append("/setsquares");
            sb.append(", ");
            sb.append("/setbet");
            sb.append(", ");
            sb.append("/setmaxtokens");
            sb.append(", ");
            sb.append("/setbalance");

            sb.append("\n");

            // Game
            sb.append("/play");

            SocketStreams.send(getOuter(), sb.toString());
        }

        /**
         *
         */
        public void leaveGroup() {
            if (getChannel() != null) {
                SocketStreams.send(getOuter(), "You can't leave a group if you are in a channel");
                return;
            }

            if (getGroup() == null) {
                SocketStreams.send(getOuter(), "You are not currently in a group");
                return;
            }

            SocketStreams.sendTo(getGroup(), getOuter().getPlayer().getName() + " left the group");

            getGroup().kick(getOuter());
        }

        /**
         *
         */
        public void leaveChannel() {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You are not currently in a channel");
                return;
            }

            SocketStreams.sendTo(getChannel(), getOuter().getPlayer().getName() + " left the channel");

            getChannel().kick(getOuter());
        }

        /**
         *
         * @param argument
         * @param entry
         */
        public void joinChannel(final String argument, final String entry) {
            if (!ValidationUtils.isNumeric(argument)) {
                SocketStreams.send(getOuter(), "You didn't select a valid channel");
                return;
            }

            if (getGroup() == null) {
                SocketStreams.send(getOuter(), "You need to be in a group in order to select a channel");
                return;
            }

            if (getChannel() != null) {
                SocketStreams.send(getOuter(), "You are already in a channel");
                return;
            }

            if (getGroup().getChannels().isEmpty()) {
                SocketStreams.send(getOuter(), "This group has no channels");
                return;
            }

            int index = Integer.parseInt(argument);

            if (index < 0) {
                SocketStreams.send(getOuter(), "The channel you selected is invalid");
                return;
            }

            if (index >= getGroup().getChannels().size()) {
                SocketStreams.send(getOuter(), "The channel you selected is invalid");
                return;
            }

            Channel channel = getGroup().getChannels().get(index);

            if (GroupUtils.isBanned(channel.getBlacklist(), getOuter())) {
                SocketStreams.send(getOuter(), "You are banned from the channel");
                return;
            }

            if (channel.hasPassword()) {
                if (entry.isEmpty()) {
                    SocketStreams.send(getOuter(), "A password is required to join the channel");
                    return;
                }

                if (!entry.equals(channel.getPassword())) {
                    SocketStreams.send(getOuter(), "Wrong password");
                    return;
                }

                getGroup().addClientToChannel(channel, getOuter());
                SocketStreams.send(getOuter(), "Joined channel " + getChannel().getName());
                return;
            }

            getGroup().addClientToChannel(channel, getOuter());

            SocketStreams.sendTo(getChannel(), getOuter().getPlayer().getName() + " joined the channel");
        }

        /**
         *
         */
        public void disconnect() {
            getOuter().disconnect();
        }

        /**
         *
         * @param argument
         * @param entry
         */
        public void joinGroup(final String argument, final String entry) {
            if (!ValidationUtils.isNumeric(argument)) {
                SocketStreams.send(getOuter(), "You didn't select a valid group");
                return;
            }

            if (getGroup() != null) {
                SocketStreams.send(getOuter(), "You are already in a group");
                return;
            }

            if (Server.getInstance().getGroups().isEmpty()) {
                SocketStreams.send(getOuter(), "No groups are available");
                return;
            }

            int index = Integer.parseInt(argument);

            if (index < 0) {
                SocketStreams.send(getOuter(), "You didn't select a valid group");
                return;
            }

            if (index >= Server.getInstance().getGroups().size()) {
                SocketStreams.send(getOuter(), "You didn't select a valid group");
                return;
            }

            Group group = Server.getInstance().getGroups().get(index);

            if (GroupUtils.isBanned(group.getBlacklist(), getOuter())) {
                SocketStreams.send(getOuter(), "You are banned from the group");
                return;
            }

            if (group.hasPassword()) {
                if (entry.isEmpty()) {
                    SocketStreams.send(getOuter(), "A password is required to join the group");
                    return;
                }

                if (!entry.equals(group.getPassword())) {
                    SocketStreams.send(getOuter(), "Wrong password");
                    return;
                }

                Server.getInstance().addClientToGroup(group, getOuter());
                SocketStreams.send(getOuter(), "Joined group " + getGroup().getName());
                return;
            }

            Server.getInstance().addClientToGroup(group, getOuter());

            SocketStreams.sendTo(getGroup(), getOuter().getPlayer().getName() + " joined the group");
        }

        /**
         *
         * @param name
         * @param password
         */
        public void createGroup(final String name, final String password) {
            if (name.isEmpty()) {
                SocketStreams.send(getOuter(), "A name is required for the group");
                return;
            }

            if (!password.isEmpty()) {
                Server.getInstance().createGroup(getOuter(), name, password);
            } else {
                Server.getInstance().createGroup(getOuter(), name);
            }

            SocketStreams.send(getOuter(), "Created group " + getGroup().getName());
        }

        /**
         *
         * @param name
         * @param password
         */
        public void createChannel(final String name, final String password) {
            if (getGroup() == null) {
                SocketStreams.send(getOuter(), "You need to be in a group to create a channel");
                return;
            }

            if (name.isEmpty()) {
                SocketStreams.send(getOuter(), "A name is required for the channel");
                return;
            }

            Channel channel;

            if (!password.isEmpty()) {
                channel = getGroup().createChannel(getOuter(), name, password);
            } else {
                channel = getGroup().createChannel(getOuter(), name);
            }

            if (channel != null) {
                setChannel(channel);
                SocketStreams.send(getOuter(), "Created channel " + getChannel().getName());
            }
        }

        /**
         *
         * @param name
         */
        public void setGroupName(String name) {
            if (getOuter().getGroup() == null) {
                SocketStreams.send(getOuter(), "You need to be in a group for that");
                return;
            }

            if (!GroupUtils.isOperator(getOuter().getGroup().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this group");
                return;
            }

            if (name.isEmpty()) {
                SocketStreams.send(getOuter(), "A name is required for the group");
                return;
            }

            getOuter().getGroup().setName(name);
        }

        /**
         *
         * @param name
         */
        public void setChannelName(String name) {
            if (getOuter().getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel for that");
                return;
            }

            if (!GroupUtils.isOperator(getOuter().getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this channel");
                return;
            }

            if (name.isEmpty()) {
                SocketStreams.send(getOuter(), "A name is required for the channel");
                return;
            }

            getOuter().getChannel().setName(name);
        }

        /**
         *
         * @param password
         */
        public void setGroupPassword(String password) {
            if (getOuter().getGroup() == null) {
                SocketStreams.send(getOuter(), "You need to be in a group for that");
                return;
            }

            if (!GroupUtils.isOperator(getOuter().getGroup().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this group");
                return;
            }

            getOuter().getGroup().setName(password);
        }

        /**
         *
         * @param password
         */
        public void setChannelPassword(String password) {
            if (getOuter().getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel for that");
                return;
            }

            if (!GroupUtils.isOperator(getOuter().getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this channel");
                return;
            }

            getOuter().getChannel().setName(password);
        }

        /**
         *
         * @param argument
         */
        public void kick(final String argument) {
            if (!ValidationUtils.isNumeric(argument)) {
                SocketStreams.send(getOuter(), "You need to select the index of a player");
                return;
            }

            if (getGroup() == null && getChannel() == null) {
                SocketStreams.send(getOuter(), "You need at least to be in a group to kick a player");
                return;
            }

            int index = Integer.parseInt(argument);

            if (getChannel() != null) {
                if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                    SocketStreams.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

                if (client.equals(getOuter())) {
                    SocketStreams.send(getOuter(), "You cannot kick yourself");
                    return;
                }

                getChannel().kick(client);

                SocketStreams.send(client, "You have been kicked from the channel");

                SocketStreams.sendTo(getChannel(), client.getPlayer().getName() + " was kicked from the channel");
                return;
            }

            if (!GroupUtils.isOperator(getGroup().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this group");
                return;
            }

            PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

            getGroup().kick(client);

            SocketStreams.send(client, "You have been kicked from the channel");

            SocketStreams.sendTo(getGroup(), client.getPlayer().getName() + " was kicked from the channel");
        }

        /**
         *
         * @param argument
         */
        public void ban(final String argument) {
            if (!ValidationUtils.isNumeric(argument)) {
                SocketStreams.send(getOuter(), "You need to select the index of a player");
                return;
            }

            if (getGroup() == null && getChannel() == null) {
                SocketStreams.send(getOuter(), "You need at least to be in a group to ban a player");
                return;
            }

            int index = Integer.parseInt(argument);

            if (getChannel() != null) {
                if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                    SocketStreams.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

                if (client.equals(getOuter())) {
                    SocketStreams.send(getOuter(), "You cannot ban yourself");
                    return;
                }

                getChannel().ban(client);

                SocketStreams.send(client, "You have been banned from the channel");

                SocketStreams.sendTo(getChannel(), client.getPlayer().getName() + " was banned from the channel");
                return;
            }

            if (!GroupUtils.isOperator(getGroup().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this group");
                return;
            }

            PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

            getGroup().ban(client);

            SocketStreams.send(client, "You have been banned from the group");

            SocketStreams.sendTo(getGroup(), client.getPlayer().getName() + " was banned from the group");
        }

        /**
         *
         * @param argument
         */
        public void op(final String argument) {
            if (!ValidationUtils.isNumeric(argument)) {
                SocketStreams.send(getOuter(), "Argument is not valid");
                return;
            }

            if (getGroup() == null && getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be at least in a group for that");
                return;
            }

            int index = Integer.parseInt(argument);

            if (getChannel() != null) {
                if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                    SocketStreams.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

                if (client.equals(getOuter())) {
                    SocketStreams.send(getOuter(), "You cannot make yourself an operator");
                    return;
                }

                if (GroupUtils.isOperator(getChannel().getOperators(), client)) {
                    SocketStreams.send(getOuter(), client.getPlayer().getName() + " is already an operator");
                    return;
                }

                getChannel().op(client);

                SocketStreams.send(getOuter(), client.getPlayer().getName() + " is now an operator");

                SocketStreams.send(client, "You are now an operator");
                return;
            }

            if (!GroupUtils.isOperator(getGroup().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this group");
                return;
            }

            PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

            SocketStreams.send(getOuter(), client.getPlayer().getName() + " is now an operator");

            getGroup().op(client);

            SocketStreams.send(getOuter(), client.getPlayer().getName() + " is now an operator");

            SocketStreams.send(client, "You are now an operator");
        }

        /**
         *
         * @param argument
         */
        public void deop(final String argument) {
            if (!ValidationUtils.isNumeric(argument)) {
                SocketStreams.send(getOuter(), "Argument is not valid");
                return;
            }

            if (getGroup() == null && getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be at least in a group for that");
                return;
            }

            int index = Integer.parseInt(argument);

            if (getChannel() != null) {
                if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                    SocketStreams.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

                if (client.equals(getOuter())) {
                    SocketStreams.send(getOuter(), "You cannot remove your own operator privileges");
                    return;
                }

                if (!GroupUtils.isOperator(getChannel().getOperators(), client)) {
                    SocketStreams.send(getOuter(), client.getPlayer().getName() + " is not an operator");
                    return;
                }

                getChannel().deop(client);

                SocketStreams.send(getOuter(), client.getPlayer().getName() + " is no longer an operator");

                SocketStreams.send(client, "You are no longer an operator");
                return;
            }

            if (!GroupUtils.isOperator(getGroup().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You are not an operator of this group");
                return;
            }

            PlayerSocket client = (PlayerSocket) getChannel().getClients().get(index);

            getGroup().deop(client);

            SocketStreams.send(getOuter(), client.getPlayer().getName() + " is no longer an operator");

            SocketStreams.send(client, "You are no longer an operator");
        }

        /**
         *
         */
        public void list() {
            if (getGroup() == null && getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a group or a channel for that");
                return;
            }

            final StringBuilder sb = new StringBuilder();

            if (getChannel() != null) {
                if (getChannel().getClients().isEmpty()) {
                    return;
                }

                for (IClientSocket client : getChannel().getClients()) {
                    PlayerSocket player = (PlayerSocket) client;
                    sb.append(player.getPlayer().getName()).append(", ");
                }

                sb.delete(sb.length() - 2, sb.length());

                SocketStreams.send(getOuter(), sb.toString());
                return;
            }

            if (getGroup().getClients().isEmpty()) {
                return;
            }

            for (IClientSocket client : getGroup().getClients()) {
                PlayerSocket player = (PlayerSocket) client;
                sb.append(player.getPlayer().getName()).append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());

            SocketStreams.send(getOuter(), sb.toString());
        }

        public void unknown(final String command) {
            SocketStreams.send(getOuter(), "Unknown command: " + command);
        }

        /**
         *
         * @param name
         */
        public void setPlayerName(final String name) {
            getPlayer().setName(name);
            SocketStreams.send(getOuter(), "Name set to " + getPlayer().getName());
        }

        /**
         *
         * @param rgb
         */
        public void setColor(final String rgb) {
            if (!ValidationUtils.isValidHexaCode(rgb)) {
                SocketStreams.send(getOuter(), "You need to specify a color with the hex format #XXXXXX");
                return;
            }

            getPlayer().setColor(Color.decode(rgb));
            SocketStreams.send(getOuter(), "Color set to " + Integer.toHexString(getPlayer().getColor().getRGB()));
        }

        /**
         *
         */
        public void startGame() {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel to start a game");
                return;
            }

            if (getChannel().getGame() != null) {
                SocketStreams.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "You need to be an operator in order to start the game");
                return;
            }

            getChannel().startGame();
        }

        /**
         *
         */
        public void stopGame() {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel for that");
                return;
            }

            if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "Only an operator can stop the game");
                return;
            }

            getChannel().stopGame();
        }

        /**
         *
         * @param string
         */
        public void setSquares(final String string) {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (getChannel().getGame() != null) {
                SocketStreams.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketStreams.send(getOuter(), "You need to specify the number of squares for the board");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketStreams.send(getOuter(), "Argument is not valid");
                return;
            }

            final int squares = Integer.parseInt(string);

            if (squares <= 0) {
                SocketStreams.send(getOuter(), "Number of squares must not be 0 and must be positive");
                return;
            }

            getChannel().getSettings().getPreferences().setSquares(squares);

            SocketStreams.sendTo(getChannel(), "Squares set to " + getChannel().getSettings().getPreferences().getSquares());
        }

        /**
         *
         * @param string
         */
        public void setBet(final String string) {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (getChannel().getGame() != null) {
                SocketStreams.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketStreams.send(getOuter(), "You need to set a valid bet");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketStreams.send(getOuter(), "Argument is not valid");
                return;
            }

            final int bet = Integer.parseInt(string);

            if (bet < 0) {
                SocketStreams.send(getOuter(), "Bet must not be 0 and must be positive");
                return;
            }

            getChannel().getSettings().getPreferences().setBet(bet);

            SocketStreams.sendTo(getChannel(), "Bet set to " + getChannel().getSettings().getPreferences().getBet());
        }

        /**
         *
         * @param string
         */
        public void setMaxTokens(final String string) {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (getChannel().getGame() != null) {
                SocketStreams.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketStreams.send(getOuter(), "You need to set a valid amount of tokens");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketStreams.send(getOuter(), "Argument is not valid");
                return;
            }

            final int maxTokens = Integer.parseInt(string);

            if (maxTokens < 0) {
                SocketStreams.send(getOuter(), "Max tokens must not be 0 and must be positive");
                return;
            }

            getChannel().getSettings().getPreferences().setMaxTokens(maxTokens);

            SocketStreams.sendTo(getChannel(), "Max tokens set to " + getChannel().getSettings().getPreferences().getMaxTokens());
        }

        /**
         *
         * @param string
         */
        public void setBalance(final String string) {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (getChannel().getGame() != null) {
                SocketStreams.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(getChannel().getOperators(), getOuter())) {
                SocketStreams.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketStreams.send(getOuter(), "You need to set a valid amount");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketStreams.send(getOuter(), "Argument is not valid");
                return;
            }

            final int balance = Integer.parseInt(string);

            if (balance < 0) {
                SocketStreams.send(getOuter(), "Balance must not be 0 and must be positive");
                return;
            }

            getChannel().getSettings().getPreferences().setInitBalance(balance);

            SocketStreams.sendTo(getChannel(), "Initial Balance set to " + getChannel().getSettings().getPreferences().getInitBalance());
        }

        /**
         *
         * @param string
         */
        public void play(final String string) {
            if (getChannel() == null) {
                SocketStreams.send(getOuter(), "You need to be in a channel in order to play");
                return;
            }

            if (getChannel().getGame() == null) {
                SocketStreams.send(getOuter(), "Game hasn't started");
                return;
            }

            if (getChannel().getGame().getPlayerlist().getCurrent().getPlayer() != getOuter().getPlayer()) {
                SocketStreams.send(getOuter(), "It's not your turn");
                return;
            }

            Token token = null;

            if (!string.isEmpty()) {
                if (ValidationUtils.isNumeric(string)) {
                    final int index = Integer.parseInt(string);

                    if (index >= 0) {
                        if (index < getPlayer().countTokens()) {
                            if (getPlayer().getToken(index).getPosition() >= 0) {
                                token = getPlayer().getToken(index);
                            }
                        }
                    }
                }
            }

            getChannel().getGame().play(token);
        }

        /**
         *
         * @return
         */
        private PlayerSocket getOuter() {
            return PlayerSocket.this;
        }

    }

}
