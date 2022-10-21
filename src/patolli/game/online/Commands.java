/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online;

import java.awt.Color;
import patolli.game.Token;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;
import patolli.game.online.server.GroupUtils;
import patolli.game.online.server.Invite;
import patolli.game.online.server.Server;
import patolli.utils.SocketHelper;
import patolli.utils.ValidationUtils;

public class Commands {

    private final PlayerSocket player;

    /**
     *
     */
    public Commands(PlayerSocket player) {
        this.player = player;
    }

    /**
     *
     */
    public void help() {
        final StringBuilder sb = new StringBuilder();

        sb.append("List of commands:\n");

        String[] commandList = {
            "/help",
            "/creategroup", "/createlobby",
            "/createchannel", "/createroom",
            "/joingroup", "/joinlobby",
            "/joinchannel", "/joinroom",
            "/leavegroup", "/leavelobby",
            "/leavechannel", "/leaveroom",
            "/invite",
            "/accept",
            "/decline",
            "/disconnect",
            "/setgroupname", "/setlobbyname",
            "/setchannelname", "/setroomname",
            "/setgrouppassword", "/setlobbypassword",
            "/setchannelpassword", "/setroompassword",
            "/kick", "/ban",
            "/op", "/deop",
            "/listplayers", "/listgroups", "/listlobbies", "/listchannels", "/listrooms"
        };

        for (String command : commandList) {
            sb.append(command).append(", ");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        sb.append("\n");

        // Player
        String[] playerCommandList = {
            "/setname", "/nickname", "/name",
            "/setcolor"
        };

        for (String command : playerCommandList) {
            sb.append(command).append(", ");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        sb.append("\n");

        // Settings
        String[] settingsCommandList = {
            "/setsquares",
            "/setbet",
            "/setmaxtokens",
            "/setbalance",
            "/setmaxplayers"
        };

        for (String command : settingsCommandList) {
            sb.append(command).append(", ");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        sb.append("\n");

        // Game
        String[] gameCommandList = {
            "/startgame",
            "/stopgame",
            "/play"
        };

        for (String command : gameCommandList) {
            sb.append(command).append(", ");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        SocketHelper.send(player, sb.toString());
    }

    /**
     *
     * @param name
     * @param password
     */
    public void createGroup(final String name, final String password) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "A name is required for the group");
            return;
        }

        if (GroupUtils.findGroupByName(name) != -1) {
            SocketHelper.send(player, "A group with that name already exists");
            return;
        }

        if (!password.isEmpty()) {
            Server.getInstance().createGroup(player, name, password);
        } else {
            Server.getInstance().createGroup(player, name);
        }

        SocketHelper.send(player, "Created group " + player.getGroup().getName());
    }

    /**
     *
     * @param name
     * @param password
     */
    public void createChannel(final String name, final String password) {
        if (player.getGroup() == null) {
            SocketHelper.send(player, "You need to be in a group to create a channel");
            return;
        }

        if (name.isEmpty()) {
            SocketHelper.send(player, "A name is required for the channel");
            return;
        }

        if (GroupUtils.findChannelByName(player.getGroup(), name) != -1) {
            SocketHelper.send(player, "A channel with that name already exists");
            return;
        }

        Channel channel;

        if (!password.isEmpty()) {
            channel = player.getGroup().createChannel(player, name, password);
        } else {
            channel = player.getGroup().createChannel(player, name);
        }

        if (channel != null) {
            player.setChannel(channel);
            SocketHelper.send(player, "Created channel " + channel.getName());
        }
    }

    /**
     *
     * @param name
     * @param password
     */
    public void joinGroup(final String name, final String password) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "You didn't select a valid group");
            return;
        }

        if (player.getGroup() != null) {
            SocketHelper.send(player, "You are already in a group");
            return;
        }

        if (Server.getInstance().getGroups().isEmpty()) {
            SocketHelper.send(player, "No groups are available");
            return;
        }

        int groupIdx = GroupUtils.findGroupByName(name);

        if (groupIdx == -1) {
            SocketHelper.send(player, "Group not found");
            return;
        }

        Group group = Server.getInstance().getGroups().get(groupIdx);

        if (GroupUtils.isBanned(group.getBlacklist(), player)) {
            SocketHelper.send(player, "You are banned from the group");
            return;
        }

        if (group.hasPassword()) {
            if (password.isEmpty()) {
                SocketHelper.send(player, "A password is required to join the group");
                return;
            }

            if (!password.equals(group.getPassword())) {
                SocketHelper.send(player, "Wrong password");
                return;
            }
        }

        Server.getInstance().addClientToGroup(group, player);

        SocketHelper.send(player, "Joined group " + player.getGroup().getName());
        SocketHelper.sendTo(group, player.getPlayer().getName() + " joined the group");
    }

    /**
     *
     * @param name
     * @param password
     */
    public void joinChannel(final String name, final String password) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "You didn't select a valid channel");
            return;
        }

        if (player.getGroup() == null) {
            SocketHelper.send(player, "You need to be in a group in order to select a channel");
            return;
        }

        if (player.getChannel() != null) {
            SocketHelper.send(player, "You are already in a channel");
            return;
        }

        if (player.getGroup().getChannels().isEmpty()) {
            SocketHelper.send(player, "This group has no channels");
            return;
        }

        int channelIdx = GroupUtils.findChannelByName(player.getGroup(), (name));

        if (channelIdx == -1) {
            SocketHelper.send(player, "Channel not found");
            return;
        }

        Channel channel = player.getGroup().getChannels().get(channelIdx);

        if (GroupUtils.isBanned(channel.getBlacklist(), player)) {
            SocketHelper.send(player, "You are banned from the channel");
            return;
        }

        if (channel.hasPassword()) {
            if (password.isEmpty()) {
                SocketHelper.send(player, "A password is required to join the channel");
                return;
            }

            if (!password.equals(channel.getPassword())) {
                SocketHelper.send(player, "Wrong password");
                return;
            }
        }

        player.getGroup().addClientToChannel(channel, player);

        SocketHelper.send(player, "Joined channel " + channel.getName());
        SocketHelper.sendTo(channel, player.getPlayer().getName() + " joined the channel");
    }

    /**
     *
     */
    public void leaveGroup() {
        if (player.getChannel() != null) {
            SocketHelper.send(player, "You can't leave a group if you are in a channel");
            return;
        }

        if (player.getGroup() == null) {
            SocketHelper.send(player, "You are not currently in a group");
            return;
        }

        SocketHelper.sendTo(player.getGroup(), player.getPlayer().getName() + " left the group");

        player.getGroup().kick(player);
    }

    /**
     *
     */
    public void leaveChannel() {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You are not currently in a channel");
            return;
        }

        SocketHelper.sendTo(player.getChannel(), player.getPlayer().getName() + " left the channel");

        player.getChannel().kick(player);
    }

    /**
     *
     * @param name
     */
    public void invite(String name, String message) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "You need to specify the player you want to invite");
            return;
        }

        if (player.getGroup() == null && player.getChannel() == null) {
            SocketHelper.send(player, "You need at least to be in a group for that");
            return;
        }

        int playerIdx = GroupUtils.findClientByName(Server.getInstance().getConnections(), name);

        if (playerIdx == -1) {
            SocketHelper.send(player, "Player not found");
            return;
        }

        PlayerSocket player1 = (PlayerSocket) Server.getInstance().getConnections().get(playerIdx);

        if (player1 == player) {
            SocketHelper.send(player, "You cannot send an invite to yourself");
            return;
        }

        Invite invite = new Invite(player, player.getGroup(), player.getChannel());
        player1.addInvite(invite);
        SocketHelper.send(player, "Sent invite to player " + player1.getPlayer().getName());

        if (player.getChannel() != null) {
            SocketHelper.send(player1, "Player " + player.getPlayer().getName() + " invited you to channel " + player.getChannel().getName() + " in group " + player.getChannel().getGroup().getName() + " (/accept or /decline <name of the inviter>)" + (!message.isEmpty() ? ": " + message : ""));
        } else {
            SocketHelper.send(player1, "Player " + player.getPlayer().getName() + " invited you to group " + player.getGroup().getName() + " (/accept or /decline <name of the inviter>)" + (!message.isEmpty() ? ": " + message : ""));
        }
    }

    /**
     *
     */
    public void accept(String name) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "Argument not valid");
            return;
        }

        int inviteIdx = player.findInviteByPlayerName(name);

        if (inviteIdx == -1) {
            SocketHelper.send(player, "Invite not found");
            return;
        }

        Invite invite = player.getInvites().get(inviteIdx);

        if (invite.getChannel() != null) { //invite to channel
            if (player.getChannel() != null //if player is in a channel
                    && !player.getChannel().equals(invite.getChannel())) { //if it's not the same channel as the invite's
                if (player.getChannel().getGroup().equals(invite.getChannel().getGroup())) { //if channels are in the same group
                    leaveChannel();
                    joinChannel(invite.getChannel().getName(), invite.getChannel().getPassword());
                } else { //if channels are in different groups, move player to new group
                    leaveChannel();
                    leaveGroup();
                    joinGroup(invite.getChannel().getGroup().getName(), invite.getChannel().getGroup().getPassword());
                    joinChannel(invite.getChannel().getName(), invite.getChannel().getPassword());
                }

            }
        } else if (invite.getGroup() != null) { //invite to group
            if (player.getChannel() != null) {
                leaveChannel();
            }

            if (player.getGroup() != null) { //if player is in a group
                if (!player.getGroup().equals(invite.getGroup())) { //if it's not the same group as the invite's
                    leaveGroup();
                    joinGroup(invite.getChannel().getGroup().getName(), invite.getChannel().getGroup().getPassword());
                }
            }
        }

        PlayerSocket player1 = (PlayerSocket) invite.getSender();
        SocketHelper.send(player, "Accepted invite of player " + player1.getPlayer().getName());

        player.removeInvite(invite);
    }

    /**
     *
     */
    public void decline(String name) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "Argument not valid");
            return;
        }

        int inviteIdx = player.findInviteByPlayerName(name);

        if (inviteIdx == -1) {
            SocketHelper.send(player, "Invite not found");
            return;
        }

        Invite invite = player.getInvites().get(inviteIdx);

        PlayerSocket player1 = (PlayerSocket) invite.getSender();
        SocketHelper.send(player, "Declined invite of player " + player1.getPlayer().getName());

        player.removeInvite(invite);
    }

    /**
     *
     */
    public void disconnect() {
        player.disconnect();
    }

    /**
     *
     * @param name
     */
    public void setGroupName(String name) {
        if (player.getGroup() == null) {
            SocketHelper.send(player, "You need to be in a group for that");
            return;
        }

        if (!GroupUtils.isOperator(player.getGroup().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this group");
            return;
        }

        if (name.isEmpty()) {
            SocketHelper.send(player, "A name is required for the group");
            return;
        }

        player.getGroup().setName(name);
    }

    /**
     *
     * @param name
     */
    public void setChannelName(String name) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel for that");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this channel");
            return;
        }

        if (name.isEmpty()) {
            SocketHelper.send(player, "A name is required for the channel");
            return;
        }

        player.getChannel().setName(name);
    }

    /**
     *
     * @param password
     */
    public void setGroupPassword(String password) {
        if (player.getGroup() == null) {
            SocketHelper.send(player, "You need to be in a group for that");
            return;
        }

        if (!GroupUtils.isOperator(player.getGroup().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this group");
            return;
        }

        player.getGroup().setName(password);
    }

    /**
     *
     * @param password
     */
    public void setChannelPassword(String password) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel for that");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this channel");
            return;
        }

        player.getChannel().setName(password);
    }

    /**
     *
     * @param name
     */
    public void kick(final String name) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "You need to select a player");
            return;
        }

        if (player.getGroup() == null && this == null) {
            SocketHelper.send(player, "You need at least to be in a group to kick a player");
            return;
        }

        if (player.getChannel() != null) {
            if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
                SocketHelper.send(player, "You are not an operator of this channel");
                return;
            }

            int playerIdx = GroupUtils.findClientByName(player.getChannel().getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(player, "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) player.getChannel().getClients().get(playerIdx);

            if (client.equals(player)) {
                SocketHelper.send(player, "You cannot kick yourself");
                return;
            }

            player.getChannel().kick(client);

            SocketHelper.send(client, "You have been kicked from the channel");

            SocketHelper.sendTo(player.getChannel(), client.getPlayer().getName() + " was kicked from the channel");
            return;
        }

        if (!GroupUtils.isOperator(player.getGroup().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this group");
            return;
        }

        int playerIdx = GroupUtils.findClientByName(player.getGroup().getClients(), name);

        if (playerIdx == -1) {
            SocketHelper.send(player, "Player not found");
            return;
        }

        PlayerSocket client = (PlayerSocket) player.getGroup().getClients().get(playerIdx);

        player.getGroup().kick(client);

        SocketHelper.send(client, "You have been kicked from the channel");

        SocketHelper.sendTo(player.getGroup(), client.getPlayer().getName() + " was kicked from the channel");
    }

    /**
     *
     * @param name
     */
    public void ban(final String name) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "You need to select a player");
            return;
        }

        if (player.getGroup() == null && player.getChannel() == null) {
            SocketHelper.send(player, "You need at least to be in a group to ban a player");
            return;
        }

        if (player.getChannel() != null) {
            if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
                SocketHelper.send(player, "You are not an operator of this channel");
                return;
            }

            int playerIdx = GroupUtils.findClientByName(player.getChannel().getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(player, "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) player.getChannel().getClients().get(playerIdx);

            if (client.equals(player)) {
                SocketHelper.send(player, "You cannot ban yourself");
                return;
            }

            player.getChannel().ban(client);

            SocketHelper.send(client, "You have been banned from the channel");

            SocketHelper.sendTo(player.getChannel(), client.getPlayer().getName() + " was banned from the channel");
            return;
        }

        if (!GroupUtils.isOperator(player.getGroup().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this group");
            return;
        }

        int playerIdx = GroupUtils.findClientByName(player.getGroup().getClients(), name);

        if (playerIdx == -1) {
            SocketHelper.send(player, "Player not found");
            return;
        }

        PlayerSocket client = (PlayerSocket) player.getGroup().getClients().get(playerIdx);

        player.getGroup().ban(client);

        SocketHelper.send(client, "You have been banned from the group");

        SocketHelper.sendTo(player.getGroup(), client.getPlayer().getName() + " was banned from the group");
    }

    /**
     *
     * @param name
     */
    public void op(final String name) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "Argument is not valid");
            return;
        }

        if (player.getGroup() == null && player.getChannel() == null) {
            SocketHelper.send(player, "You need to be at least in a group for that");
            return;
        }

        if (player.getChannel() != null) {
            if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
                SocketHelper.send(player, "You are not an operator of this channel");
                return;
            }

            int playerIdx = GroupUtils.findClientByName(player.getChannel().getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(player, "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) player.getChannel().getClients().get(playerIdx);

            if (client.equals(player)) {
                SocketHelper.send(player, "You cannot make yourself an operator");
                return;
            }

            if (GroupUtils.isOperator(player.getChannel().getOperators(), client)) {
                SocketHelper.send(player, client.getPlayer().getName() + " is already an operator");
                return;
            }

            player.getChannel().op(client);

            SocketHelper.send(player, client.getPlayer().getName() + " is now an operator");

            SocketHelper.send(client, "You are now an operator");
            return;
        }

        if (!GroupUtils.isOperator(player.getGroup().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this group");
            return;
        }

        int playerIdx = GroupUtils.findClientByName(player.getGroup().getClients(), name);

        if (playerIdx == -1) {
            SocketHelper.send(player, "Player not found");
            return;
        }

        PlayerSocket client = (PlayerSocket) player.getGroup().getClients().get(playerIdx);

        SocketHelper.send(player, client.getPlayer().getName() + " is now an operator");

        player.getGroup().op(client);

        SocketHelper.send(player, client.getPlayer().getName() + " is now an operator");

        SocketHelper.send(client, "You are now an operator");
    }

    /**
     *
     * @param name
     */
    public void deop(final String name) {
        if (name.isEmpty()) {
            SocketHelper.send(player, "Argument is not valid");
            return;
        }

        if (player.getGroup() == null && player.getChannel() == null) {
            SocketHelper.send(player, "You need to be at least in a group for that");
            return;
        }

        if (player.getChannel() != null) {
            if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
                SocketHelper.send(player, "You are not an operator of this channel");
                return;
            }

            int playerIdx = GroupUtils.findClientByName(player.getChannel().getClients(), name);

            if (playerIdx == -1) {
                SocketHelper.send(player, "Player not found");
                return;
            }

            PlayerSocket client = (PlayerSocket) player.getChannel().getClients().get(playerIdx);

            if (client.equals(player)) {
                SocketHelper.send(player, "You cannot remove your own operator privileges");
                return;
            }

            if (!GroupUtils.isOperator(player.getChannel().getOperators(), client)) {
                SocketHelper.send(player, client.getPlayer().getName() + " is not an operator");
                return;
            }

            player.getChannel().deop(client);

            SocketHelper.send(player, client.getPlayer().getName() + " is no longer an operator");

            SocketHelper.send(client, "You are no longer an operator");
            return;
        }

        if (!GroupUtils.isOperator(player.getGroup().getOperators(), player)) {
            SocketHelper.send(player, "You are not an operator of this group");
            return;
        }

        int playerIdx = GroupUtils.findClientByName(player.getGroup().getClients(), name);

        if (playerIdx == -1) {
            SocketHelper.send(player, "Player not found");
            return;
        }

        PlayerSocket client = (PlayerSocket) player.getGroup().getClients().get(playerIdx);

        player.getGroup().deop(client);

        SocketHelper.send(player, client.getPlayer().getName() + " is no longer an operator");

        SocketHelper.send(client, "You are no longer an operator");
    }

    /**
     *
     */
    public void listPlayers() {
        if (player.getGroup() == null && player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a group or a channel for that");
            return;
        }

        final StringBuilder sb = new StringBuilder();

        if (player.getChannel() != null) {
            if (player.getChannel().getClients().isEmpty()) {
                return;
            }

            for (IClientSocket client : player.getChannel().getClients()) {
                PlayerSocket player1 = (PlayerSocket) client;
                sb.append(player1.getPlayer().getName()).append(", ");
            }

            sb.delete(sb.length() - 2, sb.length());

            SocketHelper.send(player, sb.toString());
            return;
        }

        if (player.getGroup().getClients().isEmpty()) {
            return;
        }

        for (IClientSocket client : player.getGroup().getClients()) {
            PlayerSocket player1 = (PlayerSocket) client;
            sb.append(player1.getPlayer().getName()).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        SocketHelper.send(player, sb.toString());
    }

    /**
     *
     */
    public void listGroups() {
        if (player.getGroup() != null || player.getChannel() != null) {
            SocketHelper.send(player, "You must not be in a group or a channel to do that");
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

        SocketHelper.send(player, sb.toString());
    }

    /**
     *
     */
    public void listChannels() {
        if (player.getGroup() != null) {
            SocketHelper.send(player, "You need to be in a group for that");
            return;
        }

        final StringBuilder sb = new StringBuilder();

        if (player.getGroup().getChannels().isEmpty()) {
            return;
        }

        for (Channel channel : player.getGroup().getChannels()) {
            sb.append(channel.getName()).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        SocketHelper.send(player, sb.toString());
    }

    /**
     *
     * @param command
     */
    public void unknown(final String command) {
        SocketHelper.send(player, "Unknown command: " + command);
    }

    /**
     *
     * @param name
     */
    public void setPlayerName(final String name) {
        player.getPlayer().setName(name);
        SocketHelper.send(player, "Name set to " + player.getPlayer().getName());
    }

    /**
     *
     * @param rgb
     */
    public void setColor(final String rgb) {
        if (!ValidationUtils.isValidHexaCode(rgb)) {
            SocketHelper.send(player, "You need to specify a color with the hex format #XXXXXX");
            return;
        }

        player.getPlayer().setColor(Color.decode(rgb));
        SocketHelper.send(player, "Color set to " + Integer.toHexString(player.getPlayer().getColor().getRGB()));
    }

    /**
     *
     */
    public void startGame() {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel to start a game");
            return;
        }

        if (player.getChannel().getGame() != null) {
            SocketHelper.send(player, "Game has already started");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "You need to be an operator in order to start the game");
            return;
        }

        player.getChannel().startGame();
    }

    /**
     *
     */
    public void stopGame() {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel for that");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "Only an operator can stop the game");
            return;
        }

        player.getChannel().stopGame();
    }

    /**
     *
     * @param string
     */
    public void setSquares(final String string) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel to change game settings");
            return;
        }

        if (player.getChannel().getGame() != null) {
            SocketHelper.send(player, "Game has already started");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(player, "You need to specify the number of squares for the board");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(player, "Argument is not valid");
            return;
        }

        final int squares = Integer.parseInt(string);

        if (squares <= 0) {
            SocketHelper.send(player, "Number of squares must not be 0 and must be positive");
            return;
        }

        player.getChannel().getGame().getPreferences().setSquares(squares);

        SocketHelper.sendTo(player.getChannel(), "Squares set to " + player.getChannel().getGame().getPreferences().getSquares());
    }

    /**
     *
     * @param string
     */
    public void setBet(final String string) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel to change game settings");
            return;
        }

        if (player.getChannel().getGame() != null) {
            SocketHelper.send(player, "Game has already started");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(player, "You need to set a valid bet");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(player, "Argument is not valid");
            return;
        }

        final int bet = Integer.parseInt(string);

        if (bet < 0) {
            SocketHelper.send(player, "Bet must not be 0 and must be positive");
            return;
        }

        player.getChannel().getGame().getPreferences().setBet(bet);

        SocketHelper.sendTo(player.getChannel(), "Bet set to " + player.getChannel().getGame().getPreferences().getBet());
    }

    /**
     *
     * @param string
     */
    public void setMaxTokens(final String string) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel to change game settings");
            return;
        }

        if (player.getChannel().getGame() != null) {
            SocketHelper.send(player, "Game has already started");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(player, "You need to set a valid amount of tokens");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(player, "Argument is not valid");
            return;
        }

        final int maxTokens = Integer.parseInt(string);

        if (maxTokens < 0) {
            SocketHelper.send(player, "Max tokens must not be 0 and must be positive");
            return;
        }

        player.getChannel().getGame().getPreferences().setMaxTokens(maxTokens);

        SocketHelper.sendTo(player.getChannel(), "Max tokens set to " + player.getChannel().getGame().getPreferences().getMaxTokens());
    }

    /**
     *
     * @param string
     */
    public void setBalance(final String string) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel to change game settings");
            return;
        }

        if (player.getChannel().getGame() != null) {
            SocketHelper.send(player, "Game has already started");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(player, "You need to set a valid amount");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(player, "Argument is not valid");
            return;
        }

        final int balance = Integer.parseInt(string);

        if (balance < 0) {
            SocketHelper.send(player, "Balance must not be 0 and must be positive");
            return;
        }

        player.getChannel().getGame().getPreferences().setInitBalance(balance);

        SocketHelper.sendTo(player.getChannel(), "Initial Balance set to " + player.getChannel().getGame().getPreferences().getInitBalance());
    }

    /**
     *
     * @param string
     */
    public void setMaxPlayers(final String string) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel to change game settings");
            return;
        }

        if (player.getChannel().getGame() != null) {
            SocketHelper.send(player, "Game has already started");
            return;
        }

        if (!GroupUtils.isOperator(player.getChannel().getOperators(), player)) {
            SocketHelper.send(player, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(player, "You need to set a valid amount of players");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(player, "Argument is not valid");
            return;
        }

        final int maxPlayers = Integer.parseInt(string);

        if (maxPlayers < 0) {
            SocketHelper.send(player, "Max players must not be 0 and must be positive");
            return;
        }

        player.getChannel().getGame().getPreferences().setMaxPlayers(maxPlayers);

        SocketHelper.sendTo(player.getChannel(), "Max players set to " + player.getChannel().getGame().getPreferences().getMaxPlayers());
    }

    /**
     *
     * @param string
     */
    public void play(final String string) {
        if (player.getChannel() == null) {
            SocketHelper.send(player, "You need to be in a channel in order to play");
            return;
        }

        if (player.getChannel().getGame() == null) {
            SocketHelper.send(player, "Game hasn't started");
            return;
        }

        if (player.getChannel().getGame().getPlayerlist().getCurrent().getPlayer() != player.getPlayer()) {
            SocketHelper.send(player, "It's not your turn");
            return;
        }

        Token token = null;

        if (!string.isEmpty()) {
            if (ValidationUtils.isNumeric(string)) {
                final int index = Integer.parseInt(string);

                if (index >= 0) {
                    if (index < player.getPlayer().tokenCount()) {
                        if (player.getPlayer().getToken(index).getPosition() >= 0) {
                            token = player.getPlayer().getToken(index);
                        }
                    }
                }
            }
        }

        player.getChannel().getGame().play(token);
    }

}
