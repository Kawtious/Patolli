/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.util.ArrayList;
import patolli.game.online.server.threads.SocketStreams;
import patolli.game.spaces.CentralSpace;
import patolli.game.spaces.ExteriorSpace;
import patolli.game.spaces.Space;
import patolli.game.spaces.SquareSpace;
import patolli.game.spaces.TriangleSpace;
import patolli.game.utils.GameUtils;
import patolli.utils.Console;

public class Board {

    private final Game game;

    private final ArrayList<Space> spaces = new ArrayList<>();

    public Board(final Game game) {
        this.game = game;
    }

    public boolean createBoard(final int squares, final int triangles) {
        if ((squares + triangles + 2) * 2 > 14) {
            return false;
        }

        for (int index = 0; index < 4; index++) {
            addBlade(squares, triangles);
            addCenters();
        }

        SocketStreams.sendTo(game.getChannel(), "Created board of size " + getSize());

        for (Space space : spaces) {
            Console.WriteLine("Board", space.toString());
        }

        return true;
    }

    private void addBlade(final int squares, final int triangles) {
        for (int side = 0; side < 2; side++) {
            if (side == 0) {
                addSquares(squares);
                addTriangles(triangles);
                addExteriors();
            } else {
                addExteriors();
                addTriangles(triangles);
                addSquares(squares);
            }
        }
    }

    private void addSquares(final int amount) {
        for (int index = 0; index < amount; index++) {
            spaces.add(new SquareSpace());
        }
    }

    private void addTriangles(final int amount) {
        for (int index = 0; index < amount; index++) {
            spaces.add(new TriangleSpace());
        }
    }

    private void addExteriors() {
        spaces.add(new ExteriorSpace());
    }

    private void addCenters() {
        spaces.add(new CentralSpace());
    }

    public int getSize() {
        return spaces.size();
    }

    public void insert(final Token token, final int pos) {
        token.setCurrentPos(pos);
        getSpace(pos).insert(token);
    }

    public void remove(final Token token) {
        getSpace(token.getCurrentPos()).remove(token);
    }

    public void removeTokensOf(final Player player) {
        for (Token token : player.getTokens()) {
            if (token.getCurrentPos() >= 0) {
                remove(token);
            }
        }
    }

    public void move(final Token token, final int nextPos) {
        int newPos = nextPos;

        if (nextPos >= getSize()) {
            newPos = nextPos - getSize();
        }

        remove(token);

        if (willTokenFinish(token, nextPos)) {
            SocketStreams.sendTo(game.getChannel(), "Token " + token.getIndex() + " of player " + token.getOwner() + " has successfully looped around the board");

            token.markAsFinished();

            GameUtils.everyonePays(game, game.getChannel().getPregame().getSettings().getBet(), game.getPlayerlist().getClients(), game.getPlayerlist().getCurrent());
        } else {
            insert(token, newPos);
        }
    }

    public boolean willCollide(final Player player, final int pos) {
        return getSpace(pos).getOwner() == null || getSpace(pos).getOwner() == player;
    }

    public boolean willTokenFinish(final Token token, final int nextPos) {
        final int initialPos = token.getInitialPos();
        final int prevPos = token.getCurrentPos();

        if (nextPos >= getSize()) {
            if (nextPos - getSize() >= initialPos) {
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

    public int getStartPos(final int turn) {
        final int boardSize = getSize();
        final int sectionSquares = boardSize / 4;
        final int blades = 4;
        final int result = (boardSize - sectionSquares * (1 + (blades - (turn + 1))));

        return result;
    }

    public Space getSpace(final int index) {
        int position = index;

        if (index >= getSize()) {
            position -= getSize();
        }

        return spaces.get(position);
    }

    public ArrayList<Space> getSpaces() {
        return spaces;
    }

}
