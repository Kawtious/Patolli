/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.client;

import dradacorus.online.client.Commands;
import dradacorus.online.client.IDragonSocket;
import dradacorus.online.server.IDragonServer;
import dradacorus.online.server.layers.LayerUtils;
import dradacorus.online.utils.SocketHelper;
import dradacorus.online.utils.ValidationUtils;
import java.awt.Color;
import patolli.game.Token;

public class PatolliCommands extends Commands {

    public PatolliCommands(IDragonServer server, IDragonSocket client) {
        super(server, client);
    }

    @Override
    public void help() {
        super.help();
        SocketHelper.send(getClient(), listExtraCommands());
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
            SocketHelper.send(getClient(), "You need to specify a color with the hex format #XXXXXX");
            return;
        }

        getClient().getPlayer().setColor(Color.decode(rgb));
        SocketHelper.send(getClient(), "Color set to " + Integer.toHexString(getClient().getPlayer().getColor().getRGB()));
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

    public void startGame() {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel to start a game");
            return;
        }

        if (getClient().getLayer().getGame() != null) {
            SocketHelper.send(getClient(), "Game has already started");
            return;
        }

        if (!LayerUtils.isOperator(getClient().getLayer().getOperators(), getClient())) {
            SocketHelper.send(getClient(), "You need to be an operator in order to start the game");
            return;
        }

        getClient().getLayer().startGame();
    }

    public void stopGame() {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel for that");
            return;
        }

        if (!LayerUtils.isOperator(getClient().getLayer().getOperators(), getClient())) {
            SocketHelper.send(getClient(), "Only an operator can stop the game");
            return;
        }

        getClient().getLayer().stopGame();
    }

    public void setSquares(String string) {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel to change game settings");
            return;
        }

        if (getClient().getLayer().getGame() != null) {
            SocketHelper.send(getClient(), "Game has already started");
            return;
        }

        if (!LayerUtils.isOperator(getClient().getLayer().getOperators(), getClient())) {
            SocketHelper.send(getClient(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(getClient(), "You need to specify the number of squares for the board");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(getClient(), "Argument is not valid");
            return;
        }

        int squares = Integer.parseInt(string);

        if (squares <= 0) {
            SocketHelper.send(getClient(), "Number of squares must not be 0 and must be positive");
            return;
        }

        getClient().getLayer().getGame().getPreferences().setSquares(squares);

        SocketHelper.sendTo(getClient().getLayer(), "Squares set to " + getClient().getLayer().getGame().getPreferences().getSquares());
    }

    public void setBet(String string) {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel to change game settings");
            return;
        }

        if (getClient().getLayer().getGame() != null) {
            SocketHelper.send(getClient(), "Game has already started");
            return;
        }

        if (!LayerUtils.isOperator(getClient().getLayer().getOperators(), getClient())) {
            SocketHelper.send(getClient(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(getClient(), "You need to set a valid bet");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(getClient(), "Argument is not valid");
            return;
        }

        final int bet = Integer.parseInt(string);

        if (bet < 0) {
            SocketHelper.send(getClient(), "Bet must not be 0 and must be positive");
            return;
        }

        getClient().getLayer().getGame().getPreferences().setBet(bet);

        SocketHelper.sendTo(getClient().getLayer(), "Bet set to " + getClient().getLayer().getGame().getPreferences().getBet());
    }

    public void setMaxTokens(String string) {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel to change game settings");
            return;
        }

        if (getClient().getLayer().getGame() != null) {
            SocketHelper.send(getClient(), "Game has already started");
            return;
        }

        if (!LayerUtils.isOperator(getClient().getLayer().getOperators(), getClient())) {
            SocketHelper.send(getClient(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(getClient(), "You need to set a valid amount of tokens");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(getClient(), "Argument is not valid");
            return;
        }

        int maxTokens = Integer.parseInt(string);

        if (maxTokens < 0) {
            SocketHelper.send(getClient(), "Max tokens must not be 0 and must be positive");
            return;
        }

        getClient().getLayer().getGame().getPreferences().setMaxTokens(maxTokens);

        SocketHelper.sendTo(getClient().getLayer(), "Max tokens set to " + getClient().getLayer().getGame().getPreferences().getMaxTokens());
    }

    public void setBalance(String string) {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel to change game settings");
            return;
        }

        if (getClient().getLayer().getGame() != null) {
            SocketHelper.send(getClient(), "Game has already started");
            return;
        }

        if (!LayerUtils.isOperator(getClient().getLayer().getOperators(), getClient())) {
            SocketHelper.send(getClient(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(getClient(), "You need to set a valid amount");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(getClient(), "Argument is not valid");
            return;
        }

        int balance = Integer.parseInt(string);

        if (balance < 0) {
            SocketHelper.send(getClient(), "Balance must not be 0 and must be positive");
            return;
        }

        getClient().getLayer().getGame().getPreferences().setInitBalance(balance);

        SocketHelper.sendTo(getClient().getLayer(), "Initial Balance set to " + getClient().getLayer().getGame().getPreferences().getInitBalance());
    }

    public void setMaxPlayers(String string) {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel to change game settings");
            return;
        }

        if (getClient().getLayer().getGame() != null) {
            SocketHelper.send(getClient(), "Game has already started");
            return;
        }

        if (!LayerUtils.isOperator(getClient().getLayer().getOperators(), getClient())) {
            SocketHelper.send(getClient(), "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.send(getClient(), "You need to set a valid amount of clients");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.send(getClient(), "Argument is not valid");
            return;
        }

        int maxPlayers = Integer.parseInt(string);

        if (maxPlayers < 0) {
            SocketHelper.send(getClient(), "Max players must not be 0 and must be positive");
            return;
        }

        getClient().getLayer().getGame().getPreferences().setMaxPlayers(maxPlayers);

        SocketHelper.sendTo(getClient().getLayer(), "Max players set to " + getClient().getLayer().getGame().getPreferences().getMaxPlayers());
    }

    public void play(String string) {
        if (getClient().getLayer() == null) {
            SocketHelper.send(getClient(), "You need to be in a channel in order to play");
            return;
        }

        if (getClient().getLayer().getGame() == null) {
            SocketHelper.send(getClient(), "Game hasn't started");
            return;
        }

        if (getClient().getLayer().getGame().getPlayerlist().getCurrent().getPlayer() != getClient().getPlayer()) {
            SocketHelper.send(getClient(), "It's not your turn");
            return;
        }

        Token token = null;

        if (!string.isEmpty()) {
            if (ValidationUtils.isNumeric(string)) {
                int index = Integer.parseInt(string);

                if (index >= 0) {
                    if (index < getClient().getPlayer().tokenCount()) {
                        if (getClient().getPlayer().getToken(index).getPosition() >= 0) {
                            token = getClient().getPlayer().getToken(index);
                        }
                    }
                }
            }
        }

        getClient().getLayer().getGame().play(token);
    }

    @Override
    public PlayerSocket getClient() {
        return (PlayerSocket) super.getClient();
    }

}
