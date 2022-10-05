/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import patolli.game.board.Board;
import patolli.game.board.spaces.CentralSpace;
import patolli.game.board.spaces.ExteriorSpace;
import patolli.game.board.spaces.Space;
import patolli.game.board.spaces.TriangleSpace;
import patolli.game.tokens.Token;
import utilities.console.Console;

public class Game {

    private Pregame pregame;

    private Board board;

    private Leaderboard leaderboard;

    private Playerlist playerlist;

    private volatile boolean finished = false;

    public Game(final Pregame pregame) {
        this.pregame = pregame;
    }

    public boolean init() {
        if (pregame.getPlayers().size() < 2) {
            Console.WriteLine("Game", "Not enough players have joined the game (" + getPlayerCount() + "/4)");
            return false;
        }

        if (!pregame.getSettings().validate()) {
            return false;
        }

        playerlist = new Playerlist(this, pregame.getPlayers());
        leaderboard = new Leaderboard(pregame.getPlayers());
        board = new Board();

        if (!board.createBoard(pregame.getSettings().getSquares(), pregame.getSettings().getTriangles())) {
            return false;
        }

        return true;
    }

    public void play(final Token token) {
        final int successes = GameUtils.countCoins(5);

        if (canPlay(token, successes)) {
            playToken(token, successes);
        }

        leaderboard.updateWinner();

        if (playerlist.getPlayers().size() < 2) {
            leaderboard.printResults();
            finished = true;
            return;
        }

        playerlist.next();
    }

    private boolean canPlay(final Token token, final int successes) {
        if (getCurrentPlayer().getBalance().isBroke()) {
            Console.WriteLine("Game", "Player " + getCurrentPlayer() + " is unable to pay any more bets and cannot continue playing");
            playerlist.remove(getCurrentPlayer());
            return false;
        }

        if (successes == 0) {
            // p a y
            if (getCurrentPlayer().tokensInPlay() != 0) {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + " is unable to move any tokens");
                GameUtils.payEveryone(getBet(), getCurrentPlayer(), getPlayers());

                getCurrentPlayer().selectNextToken();
            } else {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + "'s turn is skipped");
            }

            return false;
        }

        if (getCurrentPlayer().countTokens() < pregame.getSettings().getMaxTokens()) {
            if (getCurrentPlayer().tokensInPlay() == 0) {
                insertToken();
                return false;
            }

            if (successes == 1) {
                if (token == null) {
                    insertToken();
                    return false;
                }
            }
        } else {
            if (getCurrentPlayer().tokensInPlay() == 0) {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + " has no more tokens to play with!");
                playerlist.remove(getCurrentPlayer());
                return false;
            }
        }

        return true;
    }

    private void playToken(final Token token, final int successes) {
        Token selectedToken = token;

        if (selectedToken == null) {
            selectedToken = getCurrentPlayer().getCurrentToken();
        } else {
            if (!selectedToken.equals(getCurrentPlayer().getCurrentToken())) {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + " pays " + pregame.getSettings().getBet() + " to move token " + selectedToken.getIndex() + " at position " + selectedToken.getCurrentPos());
                GameUtils.payEveryone(getBet(), getCurrentPlayer(), getPlayers());
            }
        }

        final int nextPos = selectedToken.getCurrentPos() + GameUtils.calculateMovement(successes);

        Console.WriteLine("Game", "Token " + selectedToken.getIndex() + " of player " + getCurrentPlayerName() + " moves to space at position " + nextPos);

        moveToken(selectedToken, nextPos);
    }

    private void insertToken() {
        final Token token = getCurrentPlayer().createToken(board.getStartPos(playerlist.getTurn()));
        board.insert(token, token.getInitialPos());

        getCurrentPlayer().selectNextToken();

        Console.WriteLine("Game", "Inserted token " + token.getIndex() + " in board for player " + getCurrentPlayer().getName() + " at position " + token.getInitialPos());
    }

    private boolean tokenCanLandOnSpace(final int pos) {
        return board.getSpace(pos).getOwner() == null || board.getSpace(pos).getOwner() == getCurrentPlayer();
    }

    private void moveToken(final Token token, final int nextPos) {
        final Space nextSpace = board.getSpace(nextPos);

        if (!tokenCanLandOnSpace(nextPos)) {
            Console.WriteLine("Game", "Token " + token.getIndex() + " of player " + getCurrentPlayerName() + " moves to space occupied by " + nextSpace.getOwner().getName());

            if (nextSpace instanceof CentralSpace) {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + " destroys " + nextSpace.getOwner().getName() + "'s tokens at position " + nextPos);

                for (Token token1 : nextSpace.list()) {
                    token1.setCurrentPos(-1);
                }

                board.move(token, nextPos);
            } else {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + " returns to previous position");
            }

            getCurrentPlayer().selectNextToken();
        } else {
            board.move(token, nextPos);

            if (nextSpace instanceof ExteriorSpace) {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + " landed on an exterior space");
                playerlist.previous();
            } else if (nextSpace instanceof TriangleSpace) {
                Console.WriteLine("Game", "Player " + getCurrentPlayerName() + " landed on an triangle space");
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
        return pregame.getSettings().getBet();
    }

    private String getCurrentPlayerName() {
        return getCurrentPlayer().getName();
    }

    public boolean hasFinished() {
        return finished;
    }

    public Playerlist getPlayerlist() {
        return playerlist;
    }

    public Board getBoard() {
        return board;
    }

}
