/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.io.Serializable;
import java.util.ArrayList;
import patolli.game.online.server.Channel;
import patolli.game.online.server.threads.SocketStreams;
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

    private GameState state;

    public Game(final Channel channel) {
        this.channel = channel;
        state = GameState.WAITING;
    }

    public void init() {
        if (channel.getPregame().getPlayers().size() < 2) {
            SocketStreams.sendTo(channel, "Not enough players have joined the game (" + getPlayerCount() + "/4)");
            return;
        }

        if (!channel.getPregame().getSettings().validate()) {
            SocketStreams.sendTo(channel, "Failed to validate settings");
            return;
        }

        board = new Board();

        if (!board.createBoard(channel.getPregame().getSettings().getSquares(), channel.getPregame().getSettings().getTriangles())) {
            return;
        }

        playerlist = new Playerlist(this, channel.getPregame().getPlayers());
        leaderboard = new Leaderboard(channel.getPregame().getPlayers());

        state = GameState.PLAYING;
    }

    public void play(final Token token) {
        getCurrentPlayer().getDice().nextOutcome();

        if (canPlay(token)) {
            playToken(token);
        }

        leaderboard.updateWinner();

        if (playerlist.getPlayers().size() < 2) {
            leaderboard.printResults();
            state = GameState.FINISHED;
            return;
        }

        playerlist.next();
    }

    private boolean canPlay(final Token token) {
        final int outcome = getCurrentPlayer().getDice().getOutcome();

        if (getCurrentPlayer().getBalance().isBroke()) {
            SocketStreams.sendTo(channel, "Player " + getCurrentPlayer() + " is unable to pay any more bets and cannot continue playing");
            playerlist.remove(getCurrentPlayer());
            return false;
        }

        if (outcome == 0) {
            // p a y
            if (getCurrentPlayer().tokensInPlay() != 0) {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + " is unable to move any tokens");
                GameUtils.payEveryone(getBet(), getCurrentPlayer(), getPlayers());

                getCurrentPlayer().selectNextToken();
            } else {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + "'s turn is skipped");
            }

            return false;
        }

        if (getCurrentPlayer().countTokens() < channel.getPregame().getSettings().getMaxTokens()) {
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
        } else {
            if (getCurrentPlayer().tokensInPlay() == 0) {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + " has no more tokens to play with!");
                playerlist.remove(getCurrentPlayer());
                return false;
            }
        }

        return true;
    }

    private void playToken(final Token token) {
        Token selectedToken = token;

        if (selectedToken == null) {
            selectedToken = getCurrentPlayer().getCurrentToken();
        } else {
            if (!selectedToken.equals(getCurrentPlayer().getCurrentToken())) {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + " pays " + channel.getPregame().getSettings().getBet() + " to move token " + selectedToken.getIndex() + " at position " + selectedToken.getCurrentPos());
                GameUtils.payEveryone(getBet(), getCurrentPlayer(), getPlayers());
            }
        }

        final int nextPos = selectedToken.getCurrentPos() + getCurrentPlayer().getDice().getOutcome();

        SocketStreams.sendTo(channel, "Token " + selectedToken.getIndex() + " of player " + getCurrentPlayerName() + " moves to space at position " + nextPos);

        moveToken(selectedToken, nextPos);
    }

    private void insertToken() {
        final Token token = getCurrentPlayer().createToken(board.getStartPos(playerlist.getTurn()));
        board.insert(token, token.getInitialPos());

        getCurrentPlayer().selectNextToken();

        SocketStreams.sendTo(channel, "Inserted token " + token.getIndex() + " in board for player " + getCurrentPlayer().getName() + " at position " + token.getInitialPos());
    }

    private boolean tokenCanLandOnSpace(final int pos) {
        return board.getSpace(pos).getOwner() == null || board.getSpace(pos).getOwner() == getCurrentPlayer();
    }

    private void moveToken(final Token token, final int nextPos) {
        final Space nextSpace = board.getSpace(nextPos);

        if (!tokenCanLandOnSpace(nextPos)) {
            SocketStreams.sendTo(channel, "Token " + token.getIndex() + " of player " + getCurrentPlayerName() + " moves to space occupied by " + nextSpace.getOwner().getName());

            if (nextSpace instanceof CentralSpace) {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + " destroys " + nextSpace.getOwner().getName() + "'s tokens at position " + nextPos);

                for (Token token1 : nextSpace.list()) {
                    token1.setCurrentPos(-1);
                }

                board.move(token, nextPos);
            } else {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + " returns to previous position");
            }

            getCurrentPlayer().selectNextToken();
        } else {
            board.move(token, nextPos);

            if (nextSpace instanceof ExteriorSpace) {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + " landed on an exterior space");
                playerlist.previous();
            } else if (nextSpace instanceof TriangleSpace) {
                SocketStreams.sendTo(channel, "Player " + getCurrentPlayerName() + " landed on an triangle space");
                GameUtils.payEveryone(getBet() * 2, getCurrentPlayer(), getPlayers());

                getCurrentPlayer().selectNextToken();
            } else {
                getCurrentPlayer().selectNextToken();
            }
        }
    }

    private ArrayList<Player> getPlayers() {
        return playerlist.getPlayers();
    }

    private int getPlayerCount() {
        return playerlist.getPlayers().size();
    }

    public Player getCurrentPlayer() {
        return playerlist.getCurrent();
    }

    private int getBet() {
        return channel.getPregame().getSettings().getBet();
    }

    private String getCurrentPlayerName() {
        return getCurrentPlayer().getName();
    }

    public GameState getState() {
        return state;
    }

    public boolean hasFinished() {
        return state == GameState.FINISHED;
    }

    public Playerlist getPlayerlist() {
        return playerlist;
    }

    public Board getBoard() {
        return board;
    }

}
