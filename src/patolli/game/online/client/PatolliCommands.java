/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.client;

import dradacorus.online.dragon.IDragonServer;
import dradacorus.online.kobold.Commands;
import dradacorus.online.kobold.IKoboldSocket;
import dradacorus.online.server.lairs.LairUtils;
import dradacorus.online.utils.SocketHelper;
import dradacorus.online.utils.ValidationUtils;
import java.awt.Color;
import patolli.game.Token;

public class PatolliCommands extends Commands {

    public PatolliCommands(IDragonServer server, IKoboldSocket client) {
        super(server, client);
    }

    @Override
    public void help() {
        super.help();
        SocketHelper.Output.send(getKobold(), listExtraCommands());
    }

    public String listExtraCommands() {
        StringBuilder sb = new StringBuilder();

        // Player
        String[] playerCommandList = {
            "/setcolor"
        };

        for (String command : playerCommandList) {
            sb.append(command).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

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

        sb.delete(sb.length() - 2, sb.length());

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

        sb.delete(sb.length() - 2, sb.length());

        sb.append("\n");

        return sb.toString();
    }

    public void setColor(String rgb) {
        if (!isValidHexaCode(rgb)) {
            SocketHelper.Output.send(getKobold(), "You need to specify a color with the hex format #XXXXXX");
            return;
        }

        getKobold().getPlayer().setColor(Color.decode(rgb));
        SocketHelper.Output.send(getKobold(), "Color set to " + Integer.toHexString(getKobold().getPlayer().getColor().getRGB()));
    }

    public void startGame() {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel to start a game");
            return;
        }

        if (getKobold().getLair().getGame() != null) {
            SocketHelper.Output.send(getKobold(), "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(getKobold().getLair().getOperators(), getKobold())) {
            SocketHelper.Output.send(getKobold(), "You need to be an operator in order to start the game");
            return;
        }

        getKobold().getLair().startGame();
    }

    public void stopGame() {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel for that");
            return;
        }

        if (!LairUtils.isOperator(getKobold().getLair().getOperators(), getKobold())) {
            SocketHelper.Output.send(getKobold(), "Only an operator can stop the game");
            return;
        }

        getKobold().getLair().stopGame();
    }

    public void setSquares(String string) {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel to change game settings");
            return;
        }

        if (getKobold().getLair().getGame() != null) {
            SocketHelper.Output.send(getKobold(), "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(getKobold().getLair().getOperators(), getKobold())) {
            SocketHelper.Output.send(getKobold(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(getKobold(), "You need to specify the number of squares for the board");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(getKobold(), "Argument is not valid");
            return;
        }

        int squares = Integer.parseInt(string);

        if (squares <= 0) {
            SocketHelper.Output.send(getKobold(), "Number of squares must not be 0 and must be positive");
            return;
        }

        getKobold().getLair().getGame().getPreferences().setSquares(squares);

        SocketHelper.Output.sendTo(getKobold().getLair(), "Squares set to " + getKobold().getLair().getGame().getPreferences().getSquares());
    }

    public void setBet(String string) {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel to change game settings");
            return;
        }

        if (getKobold().getLair().getGame() != null) {
            SocketHelper.Output.send(getKobold(), "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(getKobold().getLair().getOperators(), getKobold())) {
            SocketHelper.Output.send(getKobold(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(getKobold(), "You need to set a valid bet");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(getKobold(), "Argument is not valid");
            return;
        }

        final int bet = Integer.parseInt(string);

        if (bet < 0) {
            SocketHelper.Output.send(getKobold(), "Bet must not be 0 and must be positive");
            return;
        }

        getKobold().getLair().getGame().getPreferences().setBet(bet);

        SocketHelper.Output.sendTo(getKobold().getLair(), "Bet set to " + getKobold().getLair().getGame().getPreferences().getBet());
    }

    public void setMaxTokens(String string) {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel to change game settings");
            return;
        }

        if (getKobold().getLair().getGame() != null) {
            SocketHelper.Output.send(getKobold(), "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(getKobold().getLair().getOperators(), getKobold())) {
            SocketHelper.Output.send(getKobold(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(getKobold(), "You need to set a valid amount of tokens");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(getKobold(), "Argument is not valid");
            return;
        }

        int maxTokens = Integer.parseInt(string);

        if (maxTokens < 0) {
            SocketHelper.Output.send(getKobold(), "Max tokens must not be 0 and must be positive");
            return;
        }

        getKobold().getLair().getGame().getPreferences().setMaxTokens(maxTokens);

        SocketHelper.Output.sendTo(getKobold().getLair(), "Max tokens set to " + getKobold().getLair().getGame().getPreferences().getMaxTokens());
    }

    public void setBalance(String string) {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel to change game settings");
            return;
        }

        if (getKobold().getLair().getGame() != null) {
            SocketHelper.Output.send(getKobold(), "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(getKobold().getLair().getOperators(), getKobold())) {
            SocketHelper.Output.send(getKobold(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(getKobold(), "You need to set a valid amount");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(getKobold(), "Argument is not valid");
            return;
        }

        int balance = Integer.parseInt(string);

        if (balance < 0) {
            SocketHelper.Output.send(getKobold(), "Balance must not be 0 and must be positive");
            return;
        }

        getKobold().getLair().getGame().getPreferences().setInitBalance(balance);

        SocketHelper.Output.sendTo(getKobold().getLair(), "Initial Balance set to " + getKobold().getLair().getGame().getPreferences().getInitBalance());
    }

    public void setMaxPlayers(String string) {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel to change game settings");
            return;
        }

        if (getKobold().getLair().getGame() != null) {
            SocketHelper.Output.send(getKobold(), "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(getKobold().getLair().getOperators(), getKobold())) {
            SocketHelper.Output.send(getKobold(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(getKobold(), "You need to set a valid amount of clients");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(getKobold(), "Argument is not valid");
            return;
        }

        int maxPlayers = Integer.parseInt(string);

        if (maxPlayers < 0) {
            SocketHelper.Output.send(getKobold(), "Max players must not be 0 and must be positive");
            return;
        }

        getKobold().getLair().getGame().getPreferences().setMaxPlayers(maxPlayers);

        SocketHelper.Output.sendTo(getKobold().getLair(), "Max players set to " + getKobold().getLair().getGame().getPreferences().getMaxPlayers());
    }

    public void play(String string) {
        if (getKobold().getLair() == null) {
            SocketHelper.Output.send(getKobold(), "You need to be in a channel in order to play");
            return;
        }

        if (getKobold().getLair().getGame() == null) {
            SocketHelper.Output.send(getKobold(), "Game hasn't started");
            return;
        }

        if (getKobold().getLair().getGame().getPlayerlist().getCurrent().getPlayer() != getKobold().getPlayer()) {
            SocketHelper.Output.send(getKobold(), "It's not your turn");
            return;
        }

        Token token = null;

        if (!string.isEmpty()) {
            if (ValidationUtils.isNumeric(string)) {
                int index = Integer.parseInt(string);

                if (index >= 0) {
                    if (index < getKobold().getPlayer().tokenCount()) {
                        if (getKobold().getPlayer().getToken(index).getPosition() >= 0) {
                            token = getKobold().getPlayer().getToken(index);
                        }
                    }
                }
            }
        }

        getKobold().getLair().getGame().play(token);
    }

    /**
     * Function to validate hexadecimal color code
     *
     * @param str
     * @return
     */
    private boolean isValidHexaCode(String str) {
        // Regex to check valid hexadecimal color code.
        return str.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }

    @Override
    public PlayerSocket getKobold() {
        return (PlayerSocket) super.getKobold();
    }

}
