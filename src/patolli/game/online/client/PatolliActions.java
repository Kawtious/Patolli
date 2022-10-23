/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.online.client;

import dradacorus.online.ExtendableLairActions;
import dradacorus.online.IDragonServer;
import dradacorus.online.IKoboldSocket;
import dradacorus.online.utils.LairUtils;
import dradacorus.online.utils.SocketHelper;
import dradacorus.online.utils.ValidationUtils;
import java.awt.Color;
import java.util.List;
import patolli.game.Token;

public class PatolliActions extends ExtendableLairActions {

    public PatolliActions(IDragonServer server) {
        super(server);
    }

    @Override
    public void help(IKoboldSocket kobold) {
        super.help(kobold);
        SocketHelper.Output.send(kobold, listExtraCommands());
    }

    @Override
    public void executeAction(IKoboldSocket kobold, String input) {
        List<String> arguments = getArguments(input);
        String execute = getArgument(arguments, 0);
        PlayerSocket player = (PlayerSocket) kobold;

        boolean modCommandExecuted = true;

        switch (execute) {
            // Player
            case "/setcolor" -> {
                setColor(player, getArgument(arguments, 1));
            }

            // Settings
            case "/setsquares" -> {
                setSquares(player, getArgument(arguments, 1));
            }
            case "/setbet" -> {
                setBet(player, getArgument(arguments, 1));
            }
            case "/setmaxtokens" -> {
                setMaxTokens(player, getArgument(arguments, 1));
            }
            case "/setbalance" -> {
                setBalance(player, getArgument(arguments, 1));
            }
            case "/setmaxplayers" -> {
                setMaxPlayers(player, getArgument(arguments, 1));
            }

            // Game
            case "/startgame" -> {
                startGame(player);
            }
            case "/stopgame" -> {
                stopGame(player);
            }
            case "/play" -> {
                play(player, getArgument(arguments, 1));
            }

            //none of the mod commands were executed
            default -> {
                modCommandExecuted = false;
            }
        }

        if (!modCommandExecuted) {
            super.executeAction(kobold, input);
        }
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
        String[] settingsActionsList = {
            "/setsquares",
            "/setbet",
            "/setmaxtokens",
            "/setbalance",
            "/setmaxplayers"
        };

        for (String action : settingsActionsList) {
            sb.append(action).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        sb.append("\n");

        // Game
        String[] gameActionsList = {
            "/startgame",
            "/stopgame",
            "/play"
        };

        for (String action : gameActionsList) {
            sb.append(action).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        sb.append("\n");

        return sb.toString();
    }

    public void setColor(PlayerSocket kobold, String rgb) {
        if (!isValidHexaCode(rgb)) {
            SocketHelper.Output.send(kobold, "You need to specify a color with the hex format #XXXXXX");
            return;
        }

        kobold.getPlayer().setColor(Color.decode(rgb));
        SocketHelper.Output.send(kobold, "Color set to " + Integer.toHexString(kobold.getPlayer().getColor().getRGB()));
    }

    public void startGame(PlayerSocket kobold) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel to start a game");
            return;
        }

        if (kobold.getLair().getGame() != null) {
            SocketHelper.Output.send(kobold, "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(kobold.getLair().getOperators(), kobold)) {
            SocketHelper.Output.send(kobold, "You need to be an operator in order to start the game");
            return;
        }

        kobold.getLair().startGame();
    }

    public void stopGame(PlayerSocket kobold) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel for that");
            return;
        }

        if (!LairUtils.isOperator(kobold.getLair().getOperators(), kobold)) {
            SocketHelper.Output.send(kobold, "Only an operator can stop the game");
            return;
        }

        kobold.getLair().stopGame();
    }

    public void setSquares(PlayerSocket kobold, String string) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel to change game settings");
            return;
        }

        if (kobold.getLair().getGame() != null) {
            SocketHelper.Output.send(kobold, "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(kobold.getLair().getOperators(), kobold)) {
            SocketHelper.Output.send(kobold, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(kobold, "You need to specify the number of squares for the board");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(kobold, "Argument is not valid");
            return;
        }

        int squares = Integer.parseInt(string);

        if (squares <= 0) {
            SocketHelper.Output.send(kobold, "Number of squares must not be 0 and must be positive");
            return;
        }

        kobold.getLair().getGame().getPreferences().setSquares(squares);

        SocketHelper.Output.sendTo(kobold.getLair(), "Squares set to " + kobold.getLair().getGame().getPreferences().getSquares());
    }

    public void setBet(PlayerSocket kobold, String string) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel to change game settings");
            return;
        }

        if (kobold.getLair().getGame() != null) {
            SocketHelper.Output.send(kobold, "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(kobold.getLair().getOperators(), kobold)) {
            SocketHelper.Output.send(kobold, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(kobold, "You need to set a valid bet");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(kobold, "Argument is not valid");
            return;
        }

        final int bet = Integer.parseInt(string);

        if (bet < 0) {
            SocketHelper.Output.send(kobold, "Bet must not be 0 and must be positive");
            return;
        }

        kobold.getLair().getGame().getPreferences().setBet(bet);

        SocketHelper.Output.sendTo(kobold.getLair(), "Bet set to " + kobold.getLair().getGame().getPreferences().getBet());
    }

    public void setMaxTokens(PlayerSocket kobold, String string) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel to change game settings");
            return;
        }

        if (kobold.getLair().getGame() != null) {
            SocketHelper.Output.send(kobold, "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(kobold.getLair().getOperators(), kobold)) {
            SocketHelper.Output.send(kobold, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(kobold, "You need to set a valid amount of tokens");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(kobold, "Argument is not valid");
            return;
        }

        int maxTokens = Integer.parseInt(string);

        if (maxTokens < 0) {
            SocketHelper.Output.send(kobold, "Max tokens must not be 0 and must be positive");
            return;
        }

        kobold.getLair().getGame().getPreferences().setMaxTokens(maxTokens);

        SocketHelper.Output.sendTo(kobold.getLair(), "Max tokens set to " + kobold.getLair().getGame().getPreferences().getMaxTokens());
    }

    public void setBalance(PlayerSocket kobold, String string) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel to change game settings");
            return;
        }

        if (kobold.getLair().getGame() != null) {
            SocketHelper.Output.send(kobold, "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(kobold.getLair().getOperators(), kobold)) {
            SocketHelper.Output.send(kobold, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(kobold, "You need to set a valid amount");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(kobold, "Argument is not valid");
            return;
        }

        int balance = Integer.parseInt(string);

        if (balance < 0) {
            SocketHelper.Output.send(kobold, "Balance must not be 0 and must be positive");
            return;
        }

        kobold.getLair().getGame().getPreferences().setInitBalance(balance);

        SocketHelper.Output.sendTo(kobold.getLair(), "Initial Balance set to " + kobold.getLair().getGame().getPreferences().getInitBalance());
    }

    public void setMaxPlayers(PlayerSocket kobold, String string) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel to change game settings");
            return;
        }

        if (kobold.getLair().getGame() != null) {
            SocketHelper.Output.send(kobold, "Game has already started");
            return;
        }

        if (!LairUtils.isOperator(kobold.getLair().getOperators(), kobold)) {
            SocketHelper.Output.send(kobold, "Only operators can change settings");
            return;
        }

        if (string.isEmpty()) {
            SocketHelper.Output.send(kobold, "You need to set a valid amount of clients");
            return;
        }

        if (!ValidationUtils.isNumeric(string)) {
            SocketHelper.Output.send(kobold, "Argument is not valid");
            return;
        }

        int maxPlayers = Integer.parseInt(string);

        if (maxPlayers < 0) {
            SocketHelper.Output.send(kobold, "Max players must not be 0 and must be positive");
            return;
        }

        kobold.getLair().getGame().getPreferences().setMaxPlayers(maxPlayers);

        SocketHelper.Output.sendTo(kobold.getLair(), "Max players set to " + kobold.getLair().getGame().getPreferences().getMaxPlayers());
    }

    public void play(PlayerSocket kobold, String string) {
        if (kobold.getLair() == null) {
            SocketHelper.Output.send(kobold, "You need to be in a channel in order to play");
            return;
        }

        if (kobold.getLair().getGame() == null) {
            SocketHelper.Output.send(kobold, "Game hasn't started");
            return;
        }

        if (kobold.getLair().getGame().getPlayerlist().getCurrent().getPlayer() != kobold.getPlayer()) {
            SocketHelper.Output.send(kobold, "It's not your turn");
            return;
        }

        Token token = null;

        if (!string.isEmpty()) {
            if (ValidationUtils.isNumeric(string)) {
                int index = Integer.parseInt(string);

                if (index >= 0) {
                    if (index < kobold.getPlayer().tokenCount()) {
                        if (kobold.getPlayer().getToken(index).getPosition() >= 0) {
                            token = kobold.getPlayer().getToken(index);
                        }
                    }
                }
            }
        }

        kobold.getLair().getGame().play(token);
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

}
