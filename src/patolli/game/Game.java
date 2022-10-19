/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.List;
import patolli.game.configuration.Settings.Preferences;
import patolli.game.online.server.Channel;
import patolli.game.online.server.threads.PlayerSocket;
import patolli.game.online.server.threads.SocketStreams;
import patolli.game.spaces.CentralSpace;
import patolli.game.spaces.ExteriorSpace;
import patolli.game.spaces.Space;
import patolli.game.spaces.TriangleSpace;

public class Game {

    private final Channel channel;

    private Board board;

    private Leaderboard leaderboard;

    private Playerlist playerlist;

    public Game(final Channel channel) {
        this.channel = channel;
    }

    public boolean init() {
        if (channel.getSettings().getPlayers().size() < 2) {
            SocketStreams.sendTo(channel, "Not enough players have joined the game");
            return false;
        }

        if (!getGameSettings().validate()) {
            SocketStreams.sendTo(channel, "Failed to validate settings");
            return false;
        }

        board = new Board();

        if (!board.createBoard(getGameSettings().getSquares())) {
            SocketStreams.sendTo(channel, "Failed to create board");
            return false;
        }

        SocketStreams.sendTo(channel, "Game is starting");

        playerlist = new Playerlist(new ArrayList<>(channel.getSettings().getPlayers()));
        leaderboard = new Leaderboard(channel.getSettings().getPlayers());

        SocketStreams.sendTo(channel, "Game has started! It is now player " + getCurrentPlayer().getName() + "'s turn");
        return true;
    }

    public void play(final Token token) {
        if (!gameHasEnded()) {
            getCurrentPlayer().getDice().nextOutcome();
            SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + " got " + getCurrentPlayer().getDice().getResult() + " after throwing the dice and can move " + getCurrentPlayer().getDice().getOutcome() + " spaces");

            if (analizeOutcome(token)) {
                playToken(token);
            }

            playerlist.nextTurn();
            SocketStreams.sendTo(channel, "It is now player " + getCurrentPlayer().getName() + "'s turn");
        } else {
            channel.stopGame();
        }
    }

    private boolean analizeOutcome(final Token token) {
        final int outcome = getCurrentPlayer().getDice().getOutcome();

        if (outcome == 0) {
            if (getCurrentPlayer().tokensInPlay() != 0) {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + " is unable to move any tokens");
                payEveryone(getGameSettings().getBet(), playerlist.getCurrent(), playerlist.getPlayers());
                return false;
            }

            SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + "'s turn is skipped");
            return false;
        }

        if (getCurrentPlayer().countTokens() < getGameSettings().getMaxTokens()) {
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
                if (getCurrentPlayer().getBalance().get() < getGameSettings().getBet()) {
                    SocketStreams.send(playerlist.getCurrent(), "Your balance is too low to select a token");
                    selectedToken = getCurrentPlayer().getCurrentToken();
                } else {
                    SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + " pays " + getGameSettings().getBet() + " to move token " + selectedToken.getIndex() + " at position " + selectedToken.getPosition());
                    payEveryone(getGameSettings().getBet(), playerlist.getCurrent(), playerlist.getPlayers());
                }
            }
        }

        final int nextPos = selectedToken.getPosition() + getCurrentPlayer().getDice().getOutcome();
        moveToken(selectedToken, nextPos);
    }

    private void moveToken(final Token token, final int nextPos) {
        final Space nextSpace = board.getSpace(nextPos);

        if (board.willTokenFinish(token, nextPos)) {
            SocketStreams.sendTo(channel, "Token " + token.getIndex() + " of player " + token.getOwner() + " has successfully looped around the board");
            token.markAsFinished();

            everyonePays(channel.getSettings().getPreferences().getBet(), playerlist.getPlayers(), playerlist.getCurrent());
            board.remove(token);
        } else {
            if (!board.willCollide(token.getOwner(), nextPos)) {
                SocketStreams.sendTo(channel, "Token " + token.getIndex() + " of player " + getCurrentPlayer().getName() + " moves to space occupied by " + nextSpace.getOwner().getName());

                if (nextSpace instanceof CentralSpace) {
                    SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + " destroys " + nextSpace.getOwner().getName() + "'s tokens at position " + nextPos);

                    for (Token token1 : nextSpace.getTokens()) {
                        token1.setPosition(-1);
                    }

                    board.move(token, nextPos);
                } else {
                    SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + " returns to previous position");
                }

                getCurrentPlayer().selectNextToken();
            } else {
                board.move(token, nextPos);

                SocketStreams.sendTo(channel, "Token " + token.getIndex() + " of player " + getCurrentPlayer().getName() + " moves to space at position " + token.getPosition());

                if (token.getPosition() >= 0) {
                    if (nextSpace instanceof ExteriorSpace) {
                        SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + " landed on an exterior space");

                        playerlist.prevTurn();
                    } else if (nextSpace instanceof TriangleSpace) {
                        SocketStreams.sendTo(channel, "Player " + getCurrentPlayer().getName() + " landed on an triangle space");

                        payEveryone(getGameSettings().getBet() * 2, playerlist.getCurrent(), playerlist.getPlayers());
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
        SocketStreams.sendTo(channel, "Inserted token " + token.getIndex() + " in board for player " + getCurrentPlayer().getName() + " at position " + token.getInitialPos());

        getCurrentPlayer().selectNextToken();
    }

    public boolean checkIfPlayerCanContinue(final PlayerSocket player) {
        if (player.getPlayer().getBalance().isBroke()) {
            SocketStreams.sendTo(channel, "Player " + player.getPlayer().getName() + " is unable to pay any more bets and cannot continue playing");
            SocketStreams.sendTo(channel, "Removing player " + player.getPlayer().getName() + " from game");

            playerlist.remove(player);
            return false;
        }

        if (player.getPlayer().countTokens() >= getGameSettings().getMaxTokens() && player.getPlayer().tokensInPlay() == 0) {
            SocketStreams.sendTo(channel, "Player " + player.getPlayer().getName() + " has no more tokens to play with!");

            playerlist.remove(player);
            return false;
        }

        return true;
    }

    public boolean gameHasEnded() {
        leaderboard.updateWinner();

        if (playerlist.getPlayers().size() < 2) {
            SocketStreams.sendTo(channel, leaderboard.printResults());
            return true;
        }

        return false;
    }

    public void pay(final int amount, final PlayerSocket from, final PlayerSocket to) {
        SocketStreams.sendTo(channel, "Player " + from.getPlayer().getName() + " pays " + amount + " to player " + to.getPlayer().getName());

        from.getPlayer().getBalance().take(amount);
        to.getPlayer().getBalance().give(amount);

        SocketStreams.sendTo(channel, "Player " + from.getPlayer().getName() + ":  " + from.getPlayer().getBalance() + " | Player " + to.getPlayer().getName() + ": " + to.getPlayer().getBalance());
    }

    public void payEveryone(final int amount, final PlayerSocket from, final List<PlayerSocket> to) {
        SocketStreams.sendTo(channel, "Player " + from.getPlayer().getName() + " pays " + amount + " to everyone");

        for (PlayerSocket player : to) {
            if (!player.equals(from)) {
                pay(amount, from, player);
            }
        }

        if (checkIfPlayerCanContinue(playerlist.getCurrent())) {
            getCurrentPlayer().selectNextToken();
            SocketStreams.sendTo(channel, "Selecting token " + getCurrentPlayer().getCurrentToken().getIndex() + " of player " + getCurrentPlayer().getName());
        }
    }

    public void everyonePays(final int amount, final List<PlayerSocket> from, final PlayerSocket to) {
        SocketStreams.sendTo(channel, "Everyone pays " + amount + " to " + to.getPlayer().getName());

        for (PlayerSocket player : from) {
            if (!player.equals(to)) {
                pay(amount, player, to);
            }
        }

        if (checkIfPlayerCanContinue(playerlist.getCurrent())) {
            getCurrentPlayer().selectNextToken();
            SocketStreams.sendTo(channel, "Selecting token " + getCurrentPlayer().getCurrentToken().getIndex() + " of player " + getCurrentPlayer().getName());
        }
    }

    private Player getCurrentPlayer() {
        return playerlist.getCurrent().getPlayer();
    }

    private Preferences getGameSettings() {
        return channel.getSettings().getPreferences();
    }

    public Channel getChannel() {
        return channel;
    }

    public Playerlist getPlayerlist() {
        return playerlist;
    }

}
