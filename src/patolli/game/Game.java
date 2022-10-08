/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.io.Serializable;
import patolli.game.online.server.Channel;
import patolli.game.online.server.threads.SocketStreams;
import patolli.game.online.server.threads.SocketThread;
import patolli.game.spaces.CentralSpace;
import patolli.game.spaces.ExteriorSpace;
import patolli.game.spaces.Space;
import patolli.game.spaces.TriangleSpace;
import patolli.game.utils.GameUtils;

public class Game implements Serializable {

    private Channel channel;

    private Board board;

    private Leaderboard leaderboard;

    private Playerlist playerlist;

    public Game(final Channel channel) {
        this.channel = channel;
    }

    public boolean init() {
        if (channel.getPregame().getPlayers().size() < 2) {
            SocketStreams.sendTo(channel, "Not enough players have joined the game (" + channel.getPregame().getPlayers().size() + "/4)");
            return false;
        }

        if (!channel.getPregame().getSettings().validate()) {
            SocketStreams.sendTo(channel, "Failed to validate settings");
            return false;
        }

        board = new Board(this);

        if (!board.createBoard(channel.getPregame().getSettings().getSquares(), channel.getPregame().getSettings().getTriangles())) {
            SocketStreams.sendTo(channel, "Failed to create board");
            return false;
        }

        SocketStreams.sendTo(channel, "Game is starting");

        playerlist = new Playerlist(this, channel.getPregame().getClients());
        leaderboard = new Leaderboard(channel.getClients());

        SocketStreams.sendTo(channel, "Game has started! It is now player " + playerlist.getCurrent().getPlayer().getName() + "'s turn");

        return true;
    }

    public void play(final Token token) {
        playerlist.getCurrent().getPlayer().getDice().nextOutcome();
        SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + " got " + playerlist.getCurrent().getPlayer().getDice().getResult() + " after throwing the dice and can move " + playerlist.getCurrent().getPlayer().getDice().getOutcome() + " spaces");

        if (analizeOutcome(token)) {
            playToken(token);
        }

        if (!gameHasEnded()) {
            playerlist.next();
            SocketStreams.sendTo(channel, "It is now player " + playerlist.getCurrent().getPlayer().getName() + "'s turn");
        } else {
            channel.stopGame();
        }
    }

    private boolean analizeOutcome(final Token token) {
        final int outcome = playerlist.getCurrent().getPlayer().getDice().getOutcome();

        if (outcome == 0) {
            // p a y
            if (playerlist.getCurrent().getPlayer().tokensInPlay() != 0) {
                SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + " is unable to move any tokens");

                GameUtils.payEveryone(this, channel.getPregame().getSettings().getBet(), playerlist.getCurrent(), playerlist.getClients());

                if (canContinue(playerlist.getCurrent())) {
                    playerlist.getCurrent().getPlayer().selectNextToken();

                    SocketStreams.sendTo(channel, "Selecting token " + playerlist.getCurrent().getPlayer().getCurrentToken().getIndex() + " of player " + playerlist.getCurrent().getPlayer().getName());
                }
            } else {
                SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + "'s turn is skipped");
            }

            return false;
        }

        if (playerlist.getCurrent().getPlayer().countTokens() < channel.getPregame().getSettings().getMaxTokens()) {
            if (playerlist.getCurrent().getPlayer().tokensInPlay() == 0) {
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

        return canContinue(playerlist.getCurrent());
    }

    private void playToken(final Token token) {
        Token selectedToken = token;

        if (selectedToken == null) {
            selectedToken = playerlist.getCurrent().getPlayer().getCurrentToken();
        } else {
            if (!selectedToken.equals(playerlist.getCurrent().getPlayer().getCurrentToken())) {
                SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + " pays " + channel.getPregame().getSettings().getBet() + " to move token " + selectedToken.getIndex() + " at position " + selectedToken.getCurrentPos());

                GameUtils.payEveryone(this, channel.getPregame().getSettings().getBet(), playerlist.getCurrent(), playerlist.getClients());
            }
        }

        if (canContinue(playerlist.getCurrent())) {
            final int nextPos = selectedToken.getCurrentPos() + playerlist.getCurrent().getPlayer().getDice().getOutcome();

            moveToken(selectedToken, nextPos);
        }
    }

    private void moveToken(final Token token, final int nextPos) {
        final Space nextSpace = board.getSpace(nextPos);

        if (!board.willCollide(token.getOwner(), nextPos)) {
            if (!board.willTokenFinish(token, nextPos)) {
                SocketStreams.sendTo(channel, "Token " + token.getIndex() + " of player " + playerlist.getCurrent().getPlayer().getName() + " moves to space occupied by " + nextSpace.getOwner().getName());

                if (nextSpace instanceof CentralSpace) {
                    SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + " destroys " + nextSpace.getOwner().getName() + "'s tokens at position " + nextPos);

                    for (Token token1 : nextSpace.list()) {
                        token1.setCurrentPos(-1);
                    }

                    board.move(token, nextPos);
                } else {
                    SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + " returns to previous position");
                }

                playerlist.getCurrent().getPlayer().selectNextToken();
            }
        } else {
            board.move(token, nextPos);

            SocketStreams.sendTo(channel, "Token " + token.getIndex() + " of player " + playerlist.getCurrent().getPlayer().getName() + " moves to space at position " + token.getCurrentPos());

            if (token.getCurrentPos() >= 0) {
                if (nextSpace instanceof ExteriorSpace) {
                    SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + " landed on an exterior space");

                    playerlist.previous();
                } else if (nextSpace instanceof TriangleSpace) {
                    SocketStreams.sendTo(channel, "Player " + playerlist.getCurrent().getPlayer().getName() + " landed on an triangle space");

                    GameUtils.payEveryone(this, channel.getPregame().getSettings().getBet() * 2, playerlist.getCurrent(), playerlist.getClients());

                    if (canContinue(playerlist.getCurrent())) {
                        playerlist.getCurrent().getPlayer().selectNextToken();
                    }
                } else {
                    playerlist.getCurrent().getPlayer().selectNextToken();
                }
            }
        }
    }

    private void insertToken() {
        final Token token = playerlist.getCurrent().getPlayer().createToken(board.getStartPos(playerlist.getTurn()));
        board.insert(token, token.getInitialPos());

        playerlist.getCurrent().getPlayer().selectNextToken();

        SocketStreams.sendTo(channel, "Inserted token " + token.getIndex() + " in board for player " + playerlist.getCurrent().getPlayer().getName() + " at position " + token.getInitialPos());
    }

    public boolean canContinue(final SocketThread client) {
        if (client.getPlayer().getBalance().isBroke()) {
            SocketStreams.sendTo(channel, "Player " + client.getPlayer().getName() + " is unable to pay any more bets and cannot continue playing");

            SocketStreams.sendTo(channel, "Removing player " + client.getPlayer().getName() + " from game");

            playerlist.remove(client, true);
            return false;
        }

        if (client.getPlayer().countTokens() >= channel.getPregame().getSettings().getMaxTokens()) {
            if (client.getPlayer().tokensInPlay() == 0) {
                SocketStreams.sendTo(channel, "Player " + client.getPlayer().getName() + " has no more tokens to play with!");

                playerlist.remove(client, false);
                return false;
            }
        }

        return true;
    }

    public boolean gameHasEnded() {
        leaderboard.updateWinner();

        if (playerlist.getClients().size() < 2) {
            SocketStreams.sendTo(channel, leaderboard.printResults());
            return true;
        }

        return false;
    }

    public Channel getChannel() {
        return channel;
    }

    public Board getBoard() {
        return board;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public Playerlist getPlayerlist() {
        return playerlist;
    }

}
