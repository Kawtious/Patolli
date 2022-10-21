/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import patolli.game.Player;
import patolli.game.online.server.Channel;
import patolli.game.online.server.Group;
import patolli.game.online.server.Invite;
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

    private final List<Invite> invites = new ArrayList<>();

    private final Commands commands = new Commands(this);

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

    public int findInviteByPlayerName(String name) {
        for (Invite invite : invites) {
            PlayerSocket sender = (PlayerSocket) invite.getSender();
            if (sender.getPlayer().getName().equals(name)) {
                return invites.indexOf(invite);
            }
        }
        return -1;
    }

    public void addInvite(Invite invite) {
        invites.add(invite);
    }

    public void removeInvite(Invite invite) {
        invites.remove(invite);
    }

    public List<Invite> getInvites() {
        return Collections.unmodifiableList(invites);
    }

    /**
     *
     * @param arguments
     * @param index
     * @return
     */
    private String getArgument(final List<String> arguments, final int index) {
        if (index < 0) {
            return "";
        }

        if (arguments.isEmpty()) {
            return "";
        }

        if (index >= arguments.size()) {
            return "";
        }

        return arguments.get(index);
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
        List<String> arguments = new ArrayList<>();

        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(message);
        while (m.find()) {
            arguments.add(m.group(1).replace("\"", ""));
        }

        final String execute = getArgument(arguments, 0);

        switch (execute) {
            case "/help", "/?" -> {
                commands.help();
            }
            case "/creategroup", "/createlobby" -> {
                commands.createGroup(getArgument(arguments, 1), getArgument(arguments, 2));
            }
            case "/createchannel", "/createroom" -> {
                commands.createChannel(getArgument(arguments, 1), getArgument(arguments, 2));
            }
            case "/joingroup", "/joinlobby" -> {
                commands.joinGroup(getArgument(arguments, 1), getArgument(arguments, 2));
            }
            case "/joinchannel", "/joinroom" -> {
                commands.joinChannel(getArgument(arguments, 1), getArgument(arguments, 2));
            }
            case "/leavegroup", "/leavelobby" -> {
                commands.leaveGroup();
            }
            case "/leavechannel", "/leaveroom" -> {
                commands.leaveChannel();
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
            case "/setgroupname", "/setlobbyname" -> {
                commands.setGroupName(getArgument(arguments, 1));
            }
            case "/setchannelname", "/setroomname" -> {
                commands.setChannelName(getArgument(arguments, 1));
            }
            case "/setgrouppassword", "/setlobbypassword" -> {
                commands.setGroupPassword(getArgument(arguments, 1));
            }
            case "/setchannelpassword", "/setroompassword" -> {
                commands.setChannelPassword(getArgument(arguments, 1));
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
                commands.setPlayerName(getArgument(arguments, 1));
            }
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
     * @param connected
     */
    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

}
