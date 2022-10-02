/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package application;

import entities.Player;
import entities.Token;
import entities.spaces.CentralSpace;
import entities.spaces.ExteriorSpace;
import entities.spaces.Space;
import entities.spaces.SquareSpace;
import entities.spaces.TriangleSpace;
import java.util.ArrayList;
import utilities.Console;

public class Board {

    private static Board instance;

    /**
     * Singleton pattern to keep a single instance of this class program running
     *
     * @return The instance of the program is returned, if there's none a new one is created
     */
    public static Board getInstance() {
        if (instance == null) {
            instance = new Board();
        }

        return instance;
    }

    private final ArrayList<Space> spaces = new ArrayList<>();

    private Board() {
    }

    public void init(final int squares, final int triangles) {
        createBoard(squares, triangles);
    }

    private void createBoard(final int squares, final int triangles) {
        resetBoard();

        for (int index = 0; index < 4; index++) {
            addBlade(squares, triangles);

            addCenterSpace();
        }

        Console.WriteLine("Created board of size " + getBoardSize());
    }

    private void addBlade(final int squares, final int triangles) {
        final int RIGHT = 0, LEFT = 1;
        for (int side = RIGHT; side < 2; side++) {
            addSquareSpaces(squares);

            if (side == RIGHT) {
                addTriangleSpaces(triangles);
                addExteriorSpace();
            } else if (side == LEFT) {
                addExteriorSpace();
                addTriangleSpaces(triangles);
            }
        }
    }

    private void addSquareSpaces(final int amount) {
        for (int index = 0; index < amount; index++) {
            spaces.add(new SquareSpace());
        }
    }

    private void addTriangleSpaces(final int amount) {
        for (int index = 0; index < amount; index++) {
            spaces.add(new TriangleSpace());
        }
    }

    private void addExteriorSpace() {
        spaces.add(new ExteriorSpace());
    }

    private void addCenterSpace() {
        spaces.add(new CentralSpace());
    }

    public int getBoardSize() {
        return spaces.size();
    }

    public void resetBoard() {
        this.spaces.clear();
    }

    public void insertNewTokenAtPos(final Token token, final int pos) {
        insertTokenAtPos(token, pos);
    }

    private void insertTokenAtPos(final Token token, final int pos) {
        token.setCurrentPos(pos);
        getSpace(pos).insertToken(token);
    }

    public void removeTokenAtPos(final Token token, final int pos) {
        getSpace(pos).removeToken(token);
    }

    private void removeTokensAtPos(final int pos) {
        getSpace(pos).clearTokens();
    }

    public void markTokenAsFinished(final Token token) {
        token.setCurrentPos(-2);
    }

    public ArrayList<Token> getTokensAtPos(final int pos) {
        return getSpace(pos).getTokens();
    }

    public Token getTokenAtPos(final int pos, final int index) {
        return getSpace(pos).getToken(index);
    }

    public int getTokenPos(final Token token) {
        return token.getCurrentPos();
    }

    public void removeTokensFromPlayer(final Player player) {
        for (Token token : player.getTokens()) {
            if (token.getCurrentPos() >= 0) {
                removeTokensAtPos(token.getCurrentPos());
            }
        }
    }

    public void moveTokenToPos(final Token token, final int nextPos) {
        int newPos = nextPos;

        if (nextPos > getBoardSize()) {
            newPos = nextPos - getBoardSize();
        }

        removeTokenAtPos(token, token.getCurrentPos());
        insertTokenAtPos(token, newPos);
    }

    public boolean willTokenFinish(final Token token, final int nextPos) {
        final int initialPos = token.getInitialPos();
        final int prevPos = token.getCurrentPos();

        if (nextPos > getBoardSize()) {
            if (nextPos - getBoardSize() >= initialPos) {
                return true;
            } else {
                return false;
            }
        }

        if (prevPos < initialPos && nextPos >= initialPos) {
            return true;
        }

        return false;
    }

    public Space getTokenSpace(final Token token) {
        return getSpace(getTokenPos(token));
    }

    public Space getSpace(final int index) {
        int position = index;

        if (index > getBoardSize()) {
            position -= getBoardSize();
        }

        return spaces.get(position - 1);
    }

    public ArrayList<Space> getSpaces() {
        return spaces;
    }

}
