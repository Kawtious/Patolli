/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.List;
import patolli.game.configuration.Settings;
import patolli.game.configuration.Settings.Preferences;
import patolli.game.online.PlayerSocket;
import patolli.game.online.server.Channel;
import patolli.game.spaces.CentralSpace;
import patolli.game.spaces.ExteriorSpace;
import patolli.game.spaces.Space;
import patolli.game.spaces.TriangleSpace;
import patolli.utils.SocketHelper;

public class Game {

    private final Channel channel;

    private final Settings settings = new Settings(new Preferences());

    private Board board;

    private Leaderboard leaderboard;

    private Playerlist playerlist;

    public Game(final Channel channel) {
        this.channel = channel;
    }

    public boolean init() {
        if (settings.getPlayers().size() < 2) {
            SocketHelper.sendTo(channel, "Not enough players have joined the game");
            return false;
        }

        if (!getPreferences().validate()) {
            SocketHelper.sendTo(channel, "Failed to validate settings");
            return false;
        }

        board = new Board();

        if (!board.createBoard(getPreferences().getSquares())) {
            SocketHelper.sendTo(channel, "Failed to create board");
            return false;
        }

        SocketHelper.sendTo(channel, "Game is starting");

        playerlist = new Playerlist(new ArrayList<>(settings.getPlayers()));
        leaderboard = new Leaderboard(settings.getPlayers());

        SocketHelper.sendTo(channel, "Game has started! It is now player " + getCurrentPlayer().getName() + "'s turn");
        return true;
    }

    public void play(final Token token) {
        if (!gameHasEnded()) {
            getCurrentPlayer().getDice().nextOutcome();
            SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + " got " + getCurrentPlayer().getDice().getResult() + " after throwing the dice and can move " + getCurrentPlayer().getDice().getOutcome() + " spaces");

            if (analizeOutcome(token)) {
                playToken(token);
            }

            playerlist.nextTurn();
            SocketHelper.sendTo(channel, "It is now player " + getCurrentPlayer().getName() + "'s turn");
        } else {
            channel.stopGame();
        }
    }

    private boolean analizeOutcome(final Token token) {
        final int outcome = getCurrentPlayer().getDice().getOutcome();

        if (outcome == 0) {
            if (getCurrentPlayer().tokensInPlay() != 0) {
                SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + " is unable to move any tokens");
                payEveryone(getPreferences().getBet(), playerlist.getCurrent(), playerlist.getPlayers());
                return false;
            }

            SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + "'s turn is skipped");
            return false;
        }

        if (getCurrentPlayer().countTokens() < getPreferences().getMaxTokens()) {
            if (getCurrentPlayer().tokensInPlay() == 0) {
                insertToken();
                return false;
            }

            if (outcome == 1) {
                if (token == null) {
                    insertToken();
                    return false;
                }
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
                if (getCurrentPlayer().getBalance().get() < getPreferences().getBet()) {
                    SocketHelper.send(playerlist.getCurrent(), "Your balance is too low to select a token");
                    selectedToken = getCurrentPlayer().getCurrentToken();
                } else {
                    SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + " pays " + getPreferences().getBet() + " to move token " + selectedToken.getIndex() + " at position " + selectedToken.getPosition());
                    payEveryone(getPreferences().getBet(), playerlist.getCurrent(), playerlist.getPlayers());
                }
            }
        }

        final int nextPos = selectedToken.getPosition() + getCurrentPlayer().getDice().getOutcome();
        moveToken(selectedToken, nextPos);
    }

    private void moveToken(final Token token, final int nextPos) {
        final Space nextSpace = board.getSpace(nextPos);

        if (board.willTokenFinish(token, nextPos)) {
            SocketHelper.sendTo(channel, "Token " + token.getIndex() + " of player " + token.getOwner() + " has successfully looped around the board");
            token.markAsFinished();

            everyonePays(settings.getPreferences().getBet(), playerlist.getPlayers(), playerlist.getCurrent());
            board.remove(token);
        } else {
            if (!board.willCollide(token.getOwner(), nextPos)) {
                SocketHelper.sendTo(channel, "Token " + token.getIndex() + " of player " + getCurrentPlayer().getName() + " moves to space occupied by " + nextSpace.getOwner().getName());

                if (nextSpace instanceof CentralSpace) {
                    SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + " destroys " + nextSpace.getOwner().getName() + "'s tokens at position " + nextPos);

                    for (Token token1 : nextSpace.getTokens()) {
                        token1.setPosition(-1);
                    }

                    board.move(token, nextPos);
                } else {
                    SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + " returns to previous position");
                }

                getCurrentPlayer().selectNextToken();
            } else {
                board.move(token, nextPos);

                SocketHelper.sendTo(channel, "Token " + token.getIndex() + " of player " + getCurrentPlayer().getName() + " moves to space at position " + token.getPosition());

                if (token.getPosition() >= 0) {
                    if (nextSpace instanceof ExteriorSpace) {
                        SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + " landed on an exterior space");

                        playerlist.prevTurn();
                    } else if (nextSpace instanceof TriangleSpace) {
                        SocketHelper.sendTo(channel, "Player " + getCurrentPlayer().getName() + " landed on an triangle space");

                        payEveryone(getPreferences().getBet() * 2, playerlist.getCurrent(), playerlist.getPlayers());
                    } else {
                        getCurrentPlayer().selectNextToken();
                    }
                }
            }
        }
    }

    private void insertToken() {
        final Token token = getCurrentPlayer().createToken(board.getStartPos(playerlist.getTurn()));
        board.insert(token, token.getInitialPos());
        SocketHelper.sendTo(channel, "Inserted token " + token.getIndex() + " in board for player " + getCurrentPlayer().getName() + " at position " + token.getInitialPos());

        getCurrentPlayer().selectNextToken();
    }

    public boolean checkIfPlayerCanContinue(final PlayerSocket player) {
        if (player.getPlayer().getBalance().isBroke()) {
            SocketHelper.sendTo(channel, "Player " + player.getPlayer().getName() + " is unable to pay any more bets and cannot continue playing");
            SocketHelper.sendTo(channel, "Removing player " + player.getPlayer().getName() + " from game");

            playerlist.remove(player);
            return false;
        }

        if (player.getPlayer().countTokens() >= getPreferences().getMaxTokens() && player.getPlayer().tokensInPlay() == 0) {
            SocketHelper.sendTo(channel, "Player " + player.getPlayer().getName() + " has no more tokens to play with!");

            playerlist.remove(player);
            return false;
        }

        return true;
    }

    public boolean gameHasEnded() {
        leaderboard.updateWinner();

        if (playerlist.getPlayers().size() < 2) {
            SocketHelper.sendTo(channel, leaderboard.printResults());
            return true;
        }

        return false;
    }

    public void pay(final int amount, final PlayerSocket from, final PlayerSocket to) {
        SocketHelper.sendTo(channel, "Player " + from.getPlayer().getName() + " pays " + amount + " to player " + to.getPlayer().getName());

        from.getPlayer().getBalance().take(amount);
        to.getPlayer().getBalance().give(amount);

        SocketHelper.sendTo(channel, "Player " + from.getPlayer().getName() + ":  " + from.getPlayer().getBalance() + " | Player " + to.getPlayer().getName() + ": " + to.getPlayer().getBalance());
    }

    public void payEveryone(final int amount, final PlayerSocket from, final List<PlayerSocket> to) {
        SocketHelper.sendTo(channel, "Player " + from.getPlayer().getName() + " pays " + amount + " to everyone");

        for (PlayerSocket player : to) {
            if (!player.equals(from)) {
                pay(amount, from, player);
            }
        }

        if (checkIfPlayerCanContinue(playerlist.getCurrent())) {
            getCurrentPlayer().selectNextToken();
            SocketHelper.sendTo(channel, "Selecting token " + getCurrentPlayer().getCurrentToken().getIndex() + " of player " + getCurrentPlayer().getName());
        }
    }

    public void everyonePays(final int amount, final List<PlayerSocket> from, final PlayerSocket to) {
        SocketHelper.sendTo(channel, "Everyone pays " + amount + " to " + to.getPlayer().getName());

        for (PlayerSocket player : from) {
            if (!player.equals(to)) {
                pay(amount, player, to);
            }
        }

        if (checkIfPlayerCanContinue(playerlist.getCurrent())) {
            getCurrentPlayer().selectNextToken();
            SocketHelper.sendTo(channel, "Selecting token " + getCurrentPlayer().getCurrentToken().getIndex() + " of player " + getCurrentPlayer().getName());
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

    public Channel getChannel() {
        return channel;
    }

    public Playerlist getPlayerlist() {
        return playerlist;
    }

}
