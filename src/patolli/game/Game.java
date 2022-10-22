/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import dradacorus.online.utils.SocketHelper;
import java.util.ArrayList;
import java.util.List;
import patolli.game.Settings.Preferences;
import patolli.game.online.client.PlayerSocket;
import patolli.game.online.server.GameLair;
import patolli.game.spaces.CentralSpace;
import patolli.game.spaces.ExteriorSpace;
import patolli.game.spaces.Space;
import patolli.game.spaces.TriangleSpace;

public class Game {

    private final GameLair gameLayer;

    private final Settings settings = new Settings(new Preferences());

    private Board board;

    private Leaderboard leaderboard;

    private Playerlist playerlist;

    public Game(final GameLair gameLayer) {
        this.gameLayer = gameLayer;
    }

    public boolean init() {
        if (settings.getPlayers().size() < 2) {
            SocketHelper.Output.sendTo(gameLayer, "Not enough players have joined the game");
            return false;
        }

        board = new Board();

        if (!board.createBoard(getPreferences().getSquares())) {
            SocketHelper.Output.sendTo(gameLayer, "Failed to create board");
            return false;
        }

        SocketHelper.Output.sendTo(gameLayer, "Game is starting");

        playerlist = new Playerlist(new ArrayList<>(settings.getPlayers()));
        leaderboard = new Leaderboard(settings.getPlayers());

        SocketHelper.Output.sendTo(gameLayer, "Game has started! It is now player " + getCurrentPlayer().getName() + "'s turn");
        return true;
    }

    public void play(final Token token) {
        if (gameHasEnded()) {
            gameLayer.stopGame();
            return;
        }

        getCurrentPlayer().getDice().nextOutcome();
        SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + " got " + getCurrentPlayer().getDice().getResult() + " after throwing the dice and can move " + getCurrentPlayer().getDice().getOutcome() + " spaces");

        if (analizeOutcome(token)) {
            playToken(token);
        }

        playerlist.nextTurn();
        SocketHelper.Output.sendTo(gameLayer, "It is now player " + getCurrentPlayer().getName() + "'s turn");
    }

    private boolean analizeOutcome(Token token) {
        int outcome = getCurrentPlayer().getDice().getOutcome();

        if (outcome == 0) {
            if (getCurrentPlayer().tokensInPlay() != 0) {
                SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + " is unable to move any tokens");
                payEveryone(getPreferences().getBet(), playerlist.getCurrent(), playerlist.getPlayers());
            } else {
                SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + "'s turn is skipped");
            }
            return false;
        }

        if (getCurrentPlayer().tokenCount() < getPreferences().getMaxTokens()) {
            if (getCurrentPlayer().tokensInPlay() == 0 || (token == null && outcome == 1)) {
                insertToken();
                return false;
            }
        }

        return checkIfPlayerCanContinue(playerlist.getCurrent());
    }

    private void playToken(final Token token) {
        Token selectedToken = token;

        if (selectedToken == null) {
            selectedToken = getCurrentPlayer().getCurrentToken();
        } else {
            if (!selectedToken.equals(getCurrentPlayer().getCurrentToken())) {
                if (getCurrentPlayer().getBalance().get() >= getPreferences().getBet() * playerlist.getPlayers().size()) {
                    SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + " pays " + getPreferences().getBet() + " to move token " + selectedToken.getIndex() + " at position " + selectedToken.getPosition());
                    payEveryone(getPreferences().getBet(), playerlist.getCurrent(), playerlist.getPlayers());
                } else {
                    SocketHelper.Output.send(playerlist.getCurrent(), "Your balance is too low to select a token");
                    selectedToken = getCurrentPlayer().getCurrentToken();
                }
            }
        }

        int nextPos = selectedToken.getPosition() + getCurrentPlayer().getDice().getOutcome();
        moveToken(selectedToken, nextPos);
    }

    private void moveToken(Token token, int nextPos) {
        Space nextSpace = board.getSpace(nextPos);

        if (board.willTokenFinish(token, nextPos)) {
            SocketHelper.Output.sendTo(gameLayer, "Token " + token.getIndex() + " of player " + token.getOwner() + " has successfully looped around the board");

            token.markAsFinished();
            everyonePays(settings.getPreferences().getBet(), playerlist.getPlayers(), playerlist.getCurrent());
            getCurrentPlayer().selectNextToken();
            board.removeToken(token);
            return;
        }

        if (board.willTokenCollideWithAnother(token.getOwner(), nextPos)) {
            SocketHelper.Output.sendTo(gameLayer, "Token " + token.getIndex() + " of player " + getCurrentPlayer().getName() + " moves to space occupied by " + nextSpace.getOwner().getName());

            if (nextSpace instanceof CentralSpace) {
                SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + " destroys " + nextSpace.getOwner().getName() + "'s tokens at position " + nextPos);
                for (Token token1 : nextSpace.getTokens()) {
                    token1.markAsDestroyed();
                }

                board.moveToken(token, nextPos);
            } else {
                SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + " returns to previous position");
            }

            getCurrentPlayer().selectNextToken();
        } else {
            board.moveToken(token, nextPos);
            SocketHelper.Output.sendTo(gameLayer, "Token " + token.getIndex() + " of player " + getCurrentPlayer().getName() + " moves to space at position " + token.getPosition());

            if (nextSpace instanceof ExteriorSpace) {
                SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + " landed on an exterior space");
                playerlist.prevTurn();
            } else if (nextSpace instanceof TriangleSpace) {
                SocketHelper.Output.sendTo(gameLayer, "Player " + getCurrentPlayer().getName() + " landed on an triangle space");
                payEveryone(getPreferences().getBet() * 2, playerlist.getCurrent(), playerlist.getPlayers());
                getCurrentPlayer().selectNextToken();
            } else {
                getCurrentPlayer().selectNextToken();
            }
        }
    }

    private void insertToken() {
        Token token = getCurrentPlayer().createToken(board.calculateTokenStartPos(playerlist.getTurn()));
        board.insertToken(token, token.getInitialPos());

        SocketHelper.Output.sendTo(gameLayer, "Inserted token " + token.getIndex() + " in board for player " + getCurrentPlayer().getName() + " at position " + token.getInitialPos());
        getCurrentPlayer().selectNextToken();
    }

    public boolean checkIfPlayerCanContinue(final PlayerSocket player) {
        if (player.getPlayer().getBalance().isBroke()) {
            SocketHelper.Output.sendTo(gameLayer, "Player " + player.getPlayer().getName() + " is unable to pay any more bets and cannot continue playing");
            SocketHelper.Output.sendTo(gameLayer, "Removing player " + player.getPlayer().getName() + " from game");

            playerlist.remove(player);
            return false;
        }

        if (player.getPlayer().tokenCount() >= getPreferences().getMaxTokens() && player.getPlayer().tokensInPlay() == 0) {
            SocketHelper.Output.sendTo(gameLayer, "Player " + player.getPlayer().getName() + " has no more tokens to play with!");
            playerlist.remove(player);
            return false;
        }

        return true;
    }

    public boolean gameHasEnded() {
        leaderboard.updateWinner();

        if (playerlist.getPlayers().size() < 2) {
            SocketHelper.Output.sendTo(gameLayer, leaderboard.printResults());
            return true;
        }

        return false;
    }

    public void pay(final int amount, final PlayerSocket from, final PlayerSocket to) {
        SocketHelper.Output.sendTo(gameLayer, "Player " + from.getPlayer().getName() + " pays " + amount + " to player " + to.getPlayer().getName());

        from.getPlayer().getBalance().take(amount);
        to.getPlayer().getBalance().give(amount);
        SocketHelper.Output.sendTo(gameLayer, "Player " + from.getPlayer().getName() + ":  " + from.getPlayer().getBalance() + " | Player " + to.getPlayer().getName() + ": " + to.getPlayer().getBalance());
    }

    public void payEveryone(final int amount, final PlayerSocket from, final List<PlayerSocket> to) {
        SocketHelper.Output.sendTo(gameLayer, "Player " + from.getPlayer().getName() + " pays " + amount + " to everyone");

        for (PlayerSocket player : to) {
            if (!player.equals(from)) {
                pay(amount, from, player);
            }
        }

        if (checkIfPlayerCanContinue(playerlist.getCurrent())) {
            getCurrentPlayer().selectNextToken();
            SocketHelper.Output.sendTo(gameLayer, "Selecting token " + getCurrentPlayer().getCurrentToken().getIndex() + " of player " + getCurrentPlayer().getName());
        }
    }

    public void everyonePays(final int amount, final List<PlayerSocket> from, final PlayerSocket to) {
        SocketHelper.Output.sendTo(gameLayer, "Everyone pays " + amount + " to " + to.getPlayer().getName());

        for (PlayerSocket player : from) {
            if (!player.equals(to)) {
                pay(amount, player, to);
            }
        }

        if (checkIfPlayerCanContinue(playerlist.getCurrent())) {
            getCurrentPlayer().selectNextToken();
            SocketHelper.Output.sendTo(gameLayer, "Selecting token " + getCurrentPlayer().getCurrentToken().getIndex() + " of player " + getCurrentPlayer().getName());
        }
    }

    private Player getCurrentPlayer() {
        return playerlist.getCurrent().getPlayer();
    }

    public Settings getSettings() {
        return settings;
    }

    public Preferences getPreferences() {
        return settings.getPreferences();
    }

    public GameLair getGameLayer() {
        return gameLayer;
    }

    public Playerlist getPlayerlist() {
        return playerlist;
    }

}
