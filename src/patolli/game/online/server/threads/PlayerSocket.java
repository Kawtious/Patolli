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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import patolli.game.Player;
import patolli.game.Token;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;
import patolli.game.online.server.GroupUtils;
import patolli.game.online.server.Server;
import patolli.utils.Console;
import patolli.utils.SocketHelper;
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
    private String getSyntax(final List<String> syntaxes, final int index) {
        if (index < 0) {
            return "";
        }

        if (syntaxes.isEmpty()) {
            return "";
        }

        if (index >= syntaxes.size()) {
            return "";
        }

        return syntaxes.get(index);
    }

    /**
     *
     * @param groups
     * @param name
     * @return
     */
    @Override
    public int findGroupByName(String name) {
        for (Group group1 : Server.getInstance().getGroups()) {
            if (group1.getName().equals(name)) {
                return Server.getInstance().getGroups().indexOf(group1);
            }
        }
        return -1;
    }

    /**
     *
     * @param channels
     * @param name
     * @return
     */
    @Override
    public int findChannelByName(String name) {
        for (Channel channel1 : group.getChannels()) {
            if (channel1.getName().equals(name)) {
                return group.getChannels().indexOf(channel1);
            }
        }
        return -1;
    }

    /**
     *
     * @param clients
     * @param name
     * @return
     */
    @Override
    public int findClientByName(List<IClientSocket> clients, String name) {
        for (IClientSocket client : clients) {
            PlayerSocket player1 = (PlayerSocket) client;
            if (player1.getPlayer().getName().equals(name)) {
                return clients.indexOf(player1);
            }
        }
        return -1;
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
            byte[] input = SocketHelper.readBytes(dis, key);

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
            if (channel != null) {
                SocketHelper.sendTo(channel, getPlayer().getName() + ": " + input);
            } else if (group != null) {
                // send messages
                SocketHelper.sendTo(group, getPlayer().getName() + ": " + input);
            }
        }
    }

    /**
     *
     * @param message
     */
    @Override
    public void executeCommand(final String message) {
        List<String> syntaxes = new ArrayList<>();

        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(message);
        while (m.find()) {
            syntaxes.add(m.group(1).replace("\"", ""));
        }

        final String execute = getSyntax(syntaxes, 0);

        switch (execute) {
            case "/help", "/?" -> {
                commands.help();
            }
            case "/creategroup", "/createlobby" -> {
                commands.createGroup(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/createchannel", "/createroom" -> {
                commands.createChannel(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/joingroup", "/joinlobby" -> {
                commands.joinGroup(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/joinchannel", "/joinroom" -> {
                commands.joinChannel(getSyntax(syntaxes, 1), getSyntax(syntaxes, 2));
            }
            case "/leavegroup", "/leavelobby" -> {
                commands.leaveGroup();
            }
            case "/leavechannel", "/leaveroom" -> {
                commands.leaveChannel();
            }
            case "/disconnect" -> {
                commands.disconnect();
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
            case "/listplayers" -> {
                commands.listPlayers();
            }
            case "/listgroups", "/listlobbies" -> {
                commands.listGroups();
            }
            case "/listchannels", "/listrooms" -> {
                commands.listChannels();
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
            sb.append("/listplayers");
            sb.append(", ");
            sb.append("/listgroups");
            sb.append(", ");
            sb.append("/listlobbies");
            sb.append(", ");
            sb.append("/listchannels");
            sb.append(", ");
            sb.append("/listrooms");

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

            SocketHelper.send(getOuter(), sb.toString());
        }

        /**
         *
         * @param name
         * @param password
         */
        public void createGroup(final String name, final String password) {
            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "A name is required for the group");
                return;
            }

            if (!password.isEmpty()) {
                Server.getInstance().createGroup(getOuter(), name, password);
            } else {
                Server.getInstance().createGroup(getOuter(), name);
            }

            SocketHelper.send(getOuter(), "Created group " + group.getName());
        }

        /**
         *
         * @param name
         * @param password
         */
        public void createChannel(final String name, final String password) {
            if (group == null) {
                SocketHelper.send(getOuter(), "You need to be in a group to create a channel");
                return;
            }

            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "A name is required for the channel");
                return;
            }

            Channel channel;

            if (!password.isEmpty()) {
                channel = group.createChannel(getOuter(), name, password);
            } else {
                channel = group.createChannel(getOuter(), name);
            }

            if (channel != null) {
                setChannel(channel);
                SocketHelper.send(getOuter(), "Created channel " + channel.getName());
            }
        }

        /**
         *
         * @param name
         * @param password
         */
        public void joinGroup(final String name, final String password) {
            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "You didn't select a valid group");
                return;
            }

            if (group != null) {
                SocketHelper.send(getOuter(), "You are already in a group");
                return;
            }

            if (Server.getInstance().getGroups().isEmpty()) {
                SocketHelper.send(getOuter(), "No groups are available");
                return;
            }

            int groupIdx = findGroupByName(name);

            if (groupIdx == -1) {
                SocketHelper.send(getOuter(), "Group not found");
                return;
            }

            Group group = Server.getInstance().getGroups().get(groupIdx);

            if (GroupUtils.isBanned(group.getBlacklist(), getOuter())) {
                SocketHelper.send(getOuter(), "You are banned from the group");
                return;
            }

            if (group.hasPassword()) {
                if (password.isEmpty()) {
                    SocketHelper.send(getOuter(), "A password is required to join the group");
                    return;
                }

                if (!password.equals(group.getPassword())) {
                    SocketHelper.send(getOuter(), "Wrong password");
                    return;
                }

                Server.getInstance().addClientToGroup(group, getOuter());
                SocketHelper.send(getOuter(), "Joined group " + group.getName());
                return;
            }

            Server.getInstance().addClientToGroup(group, getOuter());

            SocketHelper.sendTo(group, getOuter().getPlayer().getName() + " joined the group");
        }

        /**
         *
         * @param name
         * @param password
         */
        public void joinChannel(final String name, final String password) {
            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "You didn't select a valid channel");
                return;
            }

            if (group == null) {
                SocketHelper.send(getOuter(), "You need to be in a group in order to select a channel");
                return;
            }

            if (channel != null) {
                SocketHelper.send(getOuter(), "You are already in a channel");
                return;
            }

            if (group.getChannels().isEmpty()) {
                SocketHelper.send(getOuter(), "This group has no channels");
                return;
            }

            int channelIdx = findChannelByName(name);

            if (channelIdx == -1) {
                SocketHelper.send(getOuter(), "Channel not found");
                return;
            }

            Channel channel = group.getChannels().get(channelIdx);

            if (GroupUtils.isBanned(channel.getBlacklist(), getOuter())) {
                SocketHelper.send(getOuter(), "You are banned from the channel");
                return;
            }

            if (channel.hasPassword()) {
                if (password.isEmpty()) {
                    SocketHelper.send(getOuter(), "A password is required to join the channel");
                    return;
                }

                if (!password.equals(channel.getPassword())) {
                    SocketHelper.send(getOuter(), "Wrong password");
                    return;
                }

                group.addClientToChannel(channel, getOuter());
                SocketHelper.send(getOuter(), "Joined channel " + channel.getName());
                return;
            }

            group.addClientToChannel(channel, getOuter());

            SocketHelper.sendTo(channel, getOuter().getPlayer().getName() + " joined the channel");
        }

        /**
         *
         */
        public void leaveGroup() {
            if (channel != null) {
                SocketHelper.send(getOuter(), "You can't leave a group if you are in a channel");
                return;
            }

            if (group == null) {
                SocketHelper.send(getOuter(), "You are not currently in a group");
                return;
            }

            SocketHelper.sendTo(group, getOuter().getPlayer().getName() + " left the group");

            group.kick(getOuter());
        }

        /**
         *
         */
        public void leaveChannel() {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You are not currently in a channel");
                return;
            }

            SocketHelper.sendTo(channel, getOuter().getPlayer().getName() + " left the channel");

            channel.kick(getOuter());
        }

        /**
         *
         */
        public void disconnect() {
            getOuter().disconnect();
        }

        /**
         *
         * @param name
         */
        public void setGroupName(String name) {
            if (group == null) {
                SocketHelper.send(getOuter(), "You need to be in a group for that");
                return;
            }

            if (!GroupUtils.isOperator(group.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this group");
                return;
            }

            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "A name is required for the group");
                return;
            }

            group.setName(name);
        }

        /**
         *
         * @param name
         */
        public void setChannelName(String name) {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel for that");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this channel");
                return;
            }

            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "A name is required for the channel");
                return;
            }

            channel.setName(name);
        }

        /**
         *
         * @param password
         */
        public void setGroupPassword(String password) {
            if (group == null) {
                SocketHelper.send(getOuter(), "You need to be in a group for that");
                return;
            }

            if (!GroupUtils.isOperator(group.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this group");
                return;
            }

            group.setName(password);
        }

        /**
         *
         * @param password
         */
        public void setChannelPassword(String password) {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel for that");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this channel");
                return;
            }

            channel.setName(password);
        }

        /**
         *
         * @param name
         */
        public void kick(final String name) {
            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "You need to select a player");
                return;
            }

            if (group == null && this == null) {
                SocketHelper.send(getOuter(), "You need at least to be in a group to kick a player");
                return;
            }

            if (channel != null) {
                if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                    SocketHelper.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                int playerIdx = findClientByName(channel.getClients(), name);

                if (playerIdx == -1) {
                    SocketHelper.send(getOuter(), "Player not found");
                    return;
                }

                PlayerSocket client = (PlayerSocket) channel.getClients().get(playerIdx);

                if (client.equals(getOuter())) {
                    SocketHelper.send(getOuter(), "You cannot kick yourself");
                    return;
                }

                channel.kick(client);

                SocketHelper.send(client, "You have been kicked from the channel");

                SocketHelper.sendTo(channel, client.getPlayer().getName() + " was kicked from the channel");
                return;
            }

            if (!GroupUtils.isOperator(group.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this group");
                return;
            }

            int playerIdx = findClientByName(group.getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(getOuter(), "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) group.getClients().get(playerIdx);

            group.kick(client);

            SocketHelper.send(client, "You have been kicked from the channel");

            SocketHelper.sendTo(group, client.getPlayer().getName() + " was kicked from the channel");
        }

        /**
         *
         * @param name
         */
        public void ban(final String name) {
            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "You need to select a player");
                return;
            }

            if (group == null && channel == null) {
                SocketHelper.send(getOuter(), "You need at least to be in a group to ban a player");
                return;
            }

            if (channel != null) {
                if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                    SocketHelper.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                int playerIdx = findClientByName(channel.getClients(), name);

                if (playerIdx == -1) {
                    SocketHelper.send(getOuter(), "Player not found");
                    return;
                }

                PlayerSocket client = (PlayerSocket) channel.getClients().get(playerIdx);

                if (client.equals(getOuter())) {
                    SocketHelper.send(getOuter(), "You cannot ban yourself");
                    return;
                }

                channel.ban(client);

                SocketHelper.send(client, "You have been banned from the channel");

                SocketHelper.sendTo(channel, client.getPlayer().getName() + " was banned from the channel");
                return;
            }

            if (!GroupUtils.isOperator(group.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this group");
                return;
            }

            int playerIdx = findClientByName(group.getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(getOuter(), "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) group.getClients().get(playerIdx);

            group.ban(client);

            SocketHelper.send(client, "You have been banned from the group");

            SocketHelper.sendTo(group, client.getPlayer().getName() + " was banned from the group");
        }

        /**
         *
         * @param name
         */
        public void op(final String name) {
            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "Argument is not valid");
                return;
            }

            if (group == null && channel == null) {
                SocketHelper.send(getOuter(), "You need to be at least in a group for that");
                return;
            }

            if (channel != null) {
                if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                    SocketHelper.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                int playerIdx = findClientByName(channel.getClients(), name);

                if (playerIdx == -1) {
                    SocketHelper.send(getOuter(), "Player not found");
                    return;
                }

                PlayerSocket client = (PlayerSocket) channel.getClients().get(playerIdx);

                if (client.equals(getOuter())) {
                    SocketHelper.send(getOuter(), "You cannot make yourself an operator");
                    return;
                }

                if (GroupUtils.isOperator(channel.getOperators(), client)) {
                    SocketHelper.send(getOuter(), client.getPlayer().getName() + " is already an operator");
                    return;
                }

                channel.op(client);

                SocketHelper.send(getOuter(), client.getPlayer().getName() + " is now an operator");

                SocketHelper.send(client, "You are now an operator");
                return;
            }

            if (!GroupUtils.isOperator(group.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this group");
                return;
            }

            int playerIdx = findClientByName(group.getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(getOuter(), "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) group.getClients().get(playerIdx);

            SocketHelper.send(getOuter(), client.getPlayer().getName() + " is now an operator");

            group.op(client);

            SocketHelper.send(getOuter(), client.getPlayer().getName() + " is now an operator");

            SocketHelper.send(client, "You are now an operator");
        }

        /**
         *
         * @param name
         */
        public void deop(final String name) {
            if (name.isEmpty()) {
                SocketHelper.send(getOuter(), "Argument is not valid");
                return;
            }

            if (group == null && channel == null) {
                SocketHelper.send(getOuter(), "You need to be at least in a group for that");
                return;
            }

            if (channel != null) {
                if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                    SocketHelper.send(getOuter(), "You are not an operator of this channel");
                    return;
                }

                int playerIdx = findClientByName(channel.getClients(), name);

                if (playerIdx == -1) {
                    SocketHelper.send(getOuter(), "Player not found");
                    return;
                }

                PlayerSocket client = (PlayerSocket) channel.getClients().get(playerIdx);

                if (client.equals(getOuter())) {
                    SocketHelper.send(getOuter(), "You cannot remove your own operator privileges");
                    return;
                }

                if (!GroupUtils.isOperator(channel.getOperators(), client)) {
                    SocketHelper.send(getOuter(), client.getPlayer().getName() + " is not an operator");
                    return;
                }

                channel.deop(client);

                SocketHelper.send(getOuter(), client.getPlayer().getName() + " is no longer an operator");

                SocketHelper.send(client, "You are no longer an operator");
                return;
            }

            if (!GroupUtils.isOperator(group.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You are not an operator of this group");
                return;
            }

            int playerIdx = findClientByName(group.getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(getOuter(), "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) group.getClients().get(playerIdx);

            group.deop(client);

            SocketHelper.send(getOuter(), client.getPlayer().getName() + " is no longer an operator");

            SocketHelper.send(client, "You are no longer an operator");
        }

        /**
         *
         */
        public void listPlayers() {
            if (group == null && channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a group or a channel for that");
                return;
            }

            final StringBuilder sb = new StringBuilder();

            if (channel != null) {
                if (channel.getClients().isEmpty()) {
                    return;
                }

                for (IClientSocket client : channel.getClients()) {
                    PlayerSocket player = (PlayerSocket) client;
                    sb.append(player.getPlayer().getName()).append(", ");
                }

                sb.delete(sb.length() - 2, sb.length());

                SocketHelper.send(getOuter(), sb.toString());
                return;
            }

            if (group.getClients().isEmpty()) {
                return;
            }

            for (IClientSocket client : group.getClients()) {
                PlayerSocket player = (PlayerSocket) client;
                sb.append(player.getPlayer().getName()).append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());

            SocketHelper.send(getOuter(), sb.toString());
        }

        /**
         *
         */
        public void listGroups() {
            if (group != null || channel != null) {
                SocketHelper.send(getOuter(), "You must not be in a group or a channel to do that");
                return;
            }

            final StringBuilder sb = new StringBuilder();

            if (Server.getInstance().getGroups().isEmpty()) {
                return;
            }

            for (Group group : Server.getInstance().getGroups()) {
                sb.append(group.getName()).append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());

            SocketHelper.send(getOuter(), sb.toString());
        }

        /**
         *
         */
        public void listChannels() {
            if (group != null) {
                SocketHelper.send(getOuter(), "You need to be in a group for that");
                return;
            }

            final StringBuilder sb = new StringBuilder();

            if (group.getChannels().isEmpty()) {
                return;
            }

            for (Channel channel : group.getChannels()) {
                sb.append(channel.getName()).append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());

            SocketHelper.send(getOuter(), sb.toString());
        }

        /**
         *
         * @param command
         */
        public void unknown(final String command) {
            SocketHelper.send(getOuter(), "Unknown command: " + command);
        }

        /**
         *
         * @param name
         */
        public void setPlayerName(final String name) {
            getPlayer().setName(name);
            SocketHelper.send(getOuter(), "Name set to " + getPlayer().getName());
        }

        /**
         *
         * @param rgb
         */
        public void setColor(final String rgb) {
            if (!ValidationUtils.isValidHexaCode(rgb)) {
                SocketHelper.send(getOuter(), "You need to specify a color with the hex format #XXXXXX");
                return;
            }

            getPlayer().setColor(Color.decode(rgb));
            SocketHelper.send(getOuter(), "Color set to " + Integer.toHexString(getPlayer().getColor().getRGB()));
        }

        /**
         *
         */
        public void startGame() {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel to start a game");
                return;
            }

            if (channel.getGame() != null) {
                SocketHelper.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "You need to be an operator in order to start the game");
                return;
            }

            channel.startGame();
        }

        /**
         *
         */
        public void stopGame() {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel for that");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "Only an operator can stop the game");
                return;
            }

            channel.stopGame();
        }

        /**
         *
         * @param string
         */
        public void setSquares(final String string) {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (channel.getGame() != null) {
                SocketHelper.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketHelper.send(getOuter(), "You need to specify the number of squares for the board");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketHelper.send(getOuter(), "Argument is not valid");
                return;
            }

            final int squares = Integer.parseInt(string);

            if (squares <= 0) {
                SocketHelper.send(getOuter(), "Number of squares must not be 0 and must be positive");
                return;
            }

            channel.getSettings().getPreferences().setSquares(squares);

            SocketHelper.sendTo(channel, "Squares set to " + channel.getSettings().getPreferences().getSquares());
        }

        /**
         *
         * @param string
         */
        public void setBet(final String string) {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (channel.getGame() != null) {
                SocketHelper.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketHelper.send(getOuter(), "You need to set a valid bet");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketHelper.send(getOuter(), "Argument is not valid");
                return;
            }

            final int bet = Integer.parseInt(string);

            if (bet < 0) {
                SocketHelper.send(getOuter(), "Bet must not be 0 and must be positive");
                return;
            }

            channel.getSettings().getPreferences().setBet(bet);

            SocketHelper.sendTo(channel, "Bet set to " + channel.getSettings().getPreferences().getBet());
        }

        /**
         *
         * @param string
         */
        public void setMaxTokens(final String string) {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (channel.getGame() != null) {
                SocketHelper.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketHelper.send(getOuter(), "You need to set a valid amount of tokens");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketHelper.send(getOuter(), "Argument is not valid");
                return;
            }

            final int maxTokens = Integer.parseInt(string);

            if (maxTokens < 0) {
                SocketHelper.send(getOuter(), "Max tokens must not be 0 and must be positive");
                return;
            }

            channel.getSettings().getPreferences().setMaxTokens(maxTokens);

            SocketHelper.sendTo(channel, "Max tokens set to " + channel.getSettings().getPreferences().getMaxTokens());
        }

        /**
         *
         * @param string
         */
        public void setBalance(final String string) {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel to change game settings");
                return;
            }

            if (channel.getGame() != null) {
                SocketHelper.send(getOuter(), "Game has already started");
                return;
            }

            if (!GroupUtils.isOperator(channel.getOperators(), getOuter())) {
                SocketHelper.send(getOuter(), "Only operators can change settings");
                return;
            }

            if (string.isEmpty()) {
                SocketHelper.send(getOuter(), "You need to set a valid amount");
                return;
            }

            if (!ValidationUtils.isNumeric(string)) {
                SocketHelper.send(getOuter(), "Argument is not valid");
                return;
            }

            final int balance = Integer.parseInt(string);

            if (balance < 0) {
                SocketHelper.send(getOuter(), "Balance must not be 0 and must be positive");
                return;
            }

            channel.getSettings().getPreferences().setInitBalance(balance);

            SocketHelper.sendTo(channel, "Initial Balance set to " + channel.getSettings().getPreferences().getInitBalance());
        }

        /**
         *
         * @param string
         */
        public void play(final String string) {
            if (channel == null) {
                SocketHelper.send(getOuter(), "You need to be in a channel in order to play");
                return;
            }

            if (channel.getGame() == null) {
                SocketHelper.send(getOuter(), "Game hasn't started");
                return;
            }

            if (channel.getGame().getPlayerlist().getCurrent().getPlayer() != getOuter().getPlayer()) {
                SocketHelper.send(getOuter(), "It's not your turn");
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

            channel.getGame().play(token);
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
