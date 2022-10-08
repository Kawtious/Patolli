/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.io.Serializable;
import java.util.List;
import patolli.game.online.server.Channel;
import patolli.game.online.server.threads.SocketStreams;
import patolli.game.online.server.threads.SocketThread;
import patolli.game.spaces.CentralSpace;
import patolli.game.spaces.ExteriorSpace;
import patolli.game.spaces.Space;
import patolli.game.spaces.TriangleSpace;
import patolli.game.tokens.Token;
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

        board = new Board();

        if (!board.createBoard(channel.getPregame().getSettings().getSquares(), channel.getPregame().getSettings().getTriangles())) {
            SocketStreams.sendTo(channel, "Failed to create board");
            return false;
        }

        SocketStreams.sendTo(channel, "Game is starting");

        playerlist = new Playerlist(this, channel.getPregame().getClients());
        leaderboard = new Leaderboard(channel.getPregame().getClients());

        SocketStreams.sendTo(channel, "Game has started! It is now player " + getCurrentClientPlayerName() + "'s turn");

        return true;
    }

    public void play(final Token token) {
        getCurrentClient().getPlayer().getDice().nextOutcome();
        SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " throws dice and can move " + getCurrentClient().getPlayer().getDice().getOutcome() + " spaces");

        if (canPlay(token)) {
            playToken(token);
        }

        leaderboard.updateWinner();

        if (playerlist.getClients().size() < 2) {
            SocketStreams.sendTo(channel, leaderboard.printResults());
            channel.stopGame();
            return;
        }

        playerlist.next();

        SocketStreams.sendTo(channel, "It is now player " + getCurrentClientPlayerName() + "'s turn");
    }

    private boolean canPlay(final Token token) {
        final int outcome = getCurrentClient().getPlayer().getDice().getOutcome();

        if (getCurrentClient().getPlayer().getBalance().isBroke()) {
            SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " is unable to pay any more bets and cannot continue playing");

            SocketStreams.sendTo(channel, "Removing player " + getCurrentClientPlayerName() + " from game");

            playerlist.remove(getCurrentClient());

            return false;
        }

        if (outcome == 0) {
            // p a y
            if (getCurrentClient().getPlayer().tokensInPlay() != 0) {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " is unable to move any tokens");
                GameUtils.payEveryone(this, getBet(), getCurrentClient(), getPlayers());

                getCurrentClient().getPlayer().selectNextToken();

                SocketStreams.sendTo(channel, "Selecting token " + getCurrentClient().getPlayer().getCurrentToken().getIndex() + " of player " + getCurrentClientPlayerName());
            } else {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + "'s turn is skipped");
            }

            return false;
        }

        if (getCurrentClient().getPlayer().countTokens() < channel.getPregame().getSettings().getMaxTokens()) {
            if (getCurrentClient().getPlayer().tokensInPlay() == 0) {
                insertToken();
                return false;
            }

            if (outcome == 1) {
                if (token == null) {
                    insertToken();
                    return false;
                }
            }
        } else {
            if (getCurrentClient().getPlayer().tokensInPlay() == 0) {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " has no more tokens to play with!");
                playerlist.remove(getCurrentClient());
                return false;
            }
        }

        return true;
    }

    private void playToken(final Token token) {
        Token selectedToken = token;

        if (selectedToken == null) {
            selectedToken = getCurrentClient().getPlayer().getCurrentToken();
        } else {
            if (!selectedToken.equals(getCurrentClient().getPlayer().getCurrentToken())) {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " pays " + channel.getPregame().getSettings().getBet() + " to move token " + selectedToken.getIndex() + " at position " + selectedToken.getCurrentPos());
                GameUtils.payEveryone(this, getBet(), getCurrentClient(), getPlayers());
            }
        }

        final int nextPos = selectedToken.getCurrentPos() + getCurrentClient().getPlayer().getDice().getOutcome();

        SocketStreams.sendTo(channel, "Token " + selectedToken.getIndex() + " of player " + getCurrentClientPlayerName() + " moves to space at position " + nextPos);

        moveToken(selectedToken, nextPos);
    }

    private void insertToken() {
        final Token token = getCurrentClient().getPlayer().createToken(board.getStartPos(playerlist.getTurn()));
        board.insert(token, token.getInitialPos());

        getCurrentClient().getPlayer().selectNextToken();

        SocketStreams.sendTo(channel, "Inserted token " + token.getIndex() + " in board for player " + getCurrentClientPlayerName() + " at position " + token.getInitialPos());
    }

    private boolean tokenCanLandOnSpace(final int pos) {
        return board.getSpace(pos).getOwner() == null || board.getSpace(pos).getOwner() == getCurrentClient().getPlayer();
    }

    private void moveToken(final Token token, final int nextPos) {
        final Space nextSpace = board.getSpace(nextPos);

        if (!tokenCanLandOnSpace(nextPos)) {
            SocketStreams.sendTo(channel, "Token " + token.getIndex() + " of player " + getCurrentClientPlayerName() + " moves to space occupied by " + nextSpace.getOwner().getName());

            if (nextSpace instanceof CentralSpace) {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " destroys " + nextSpace.getOwner().getName() + "'s tokens at position " + nextPos);

                for (Token token1 : nextSpace.list()) {
                    token1.setCurrentPos(-1);
                }

                board.move(token, nextPos);
            } else {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " returns to previous position");
            }

            getCurrentClient().getPlayer().selectNextToken();
        } else {
            board.move(token, nextPos);

            if (nextSpace instanceof ExteriorSpace) {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " landed on an exterior space");
                playerlist.previous();
            } else if (nextSpace instanceof TriangleSpace) {
                SocketStreams.sendTo(channel, "Player " + getCurrentClientPlayerName() + " landed on an triangle space");
                GameUtils.payEveryone(this, getBet() * 2, getCurrentClient(), getPlayers());

                getCurrentClient().getPlayer().selectNextToken();
            } else {
                getCurrentClient().getPlayer().selectNextToken();
            }
        }
    }

    private List<SocketThread> getPlayers() {
        return playerlist.getClients();
    }

    public SocketThread getCurrentClient() {
        return playerlist.getCurrent();
    }

    private int getBet() {
        return channel.getPregame().getSettings().getBet();
    }

    private String getCurrentClientPlayerName() {
        return getCurrentClient().getPlayer().getName();
    }

    public Playerlist getPlayerlist() {
        return playerlist;
    }

    public Board getBoard() {
        return board;
    }

    public Channel getChannel() {
        return channel;
    }

}
