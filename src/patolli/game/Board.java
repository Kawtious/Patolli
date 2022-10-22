/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import java.util.List;
import patolli.game.spaces.CentralSpace;
import patolli.game.spaces.ExteriorSpace;
import patolli.game.spaces.Space;
import patolli.game.spaces.SquareSpace;
import patolli.game.spaces.TriangleSpace;

public class Board {

    private final List<Space> spaces = new ArrayList<>();

    public Board() {
    }

    public boolean createBoard(final int squares) {
        int bladeSize = (squares + 2 + 2) * 2;
        int maxBladeSize = 14;

        if (bladeSize > maxBladeSize) {
            return false;
        }

        for (int index = 0; index < 4; index++) {
            addBlade(squares);
            addCenters();
        }

        return true;
    }

    private void addBlade(final int squares) {
        for (int side = 0; side < 2; side++) {
            if (side == 0) {
                addSquares(squares);
                addTriangles();
                addExteriors();
            } else {
                addExteriors();
                addTriangles();
                addSquares(squares);
            }
        }
    }

    private void addSquares(final int amount) {
        for (int index = 0; index < amount; index++) {
            spaces.add(new SquareSpace());
        }
    }

    private void addTriangles() {
        for (int index = 0; index < 2; index++) {
            spaces.add(new TriangleSpace());
        }
    }

    private void addExteriors() {
        spaces.add(new ExteriorSpace());
    }

    private void addCenters() {
        spaces.add(new CentralSpace());
    }

    public void insertToken(Token token, int pos) {
        token.setPosition(pos);
        getSpace(pos).insertToken(token);
    }

    public void removeToken(Token token) {
        getSpace(token.getPosition()).removeToken(token);
    }

    public void removeTokensOf(Player player) {
        for (Token token : player.getTokens()) {
            if (token.getPosition() >= 0) {
                removeToken(token);
            }
        }
    }

    public void moveToken(Token token, int nextPos) {
        int newPos = nextPos;

        if (nextPos >= getBoardSize()) {
            newPos = nextPos - getBoardSize();
        }

        removeToken(token);

        if (!willTokenFinish(token, nextPos)) {
            insertToken(token, newPos);
        }
    }

    public boolean willTokenCollideWithAnother(Player player, int pos) {
        return getSpace(pos).getOwner() != null || getSpace(pos).getOwner() != player;
    }

    public boolean willTokenFinish(final Token token, final int nextPos) {
        final int initialPos = token.getInitialPos();
        final int prevPos = token.getPosition();

        if (nextPos >= getBoardSize()) {
            return nextPos - getBoardSize() >= initialPos;
        }

        return prevPos < initialPos && nextPos >= initialPos;
    }

    public int calculateTokenStartPos(int turn) {
        final int boardSize = getBoardSize();
        final int sectionSquares = boardSize / 4;
        final int blades = 4;
        final int result = (boardSize - sectionSquares * (1 + (blades - (turn + 1))));

        return result;
    }

    public Space getSpace(final int index) {
        int position = index;

        if (index >= getBoardSize()) {
            position -= getBoardSize();
        }

        return spaces.get(position);
    }

    public int getBoardSize() {
        return spaces.size();
    }

}
