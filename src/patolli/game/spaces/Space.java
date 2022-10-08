/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.spaces;

import java.util.ArrayList;
import patolli.game.Player;
import patolli.game.Token;

public abstract class Space {

    private final ArrayList<Token> tokens = new ArrayList<>();

    public ArrayList<Token> list() {
        return tokens;
    }

    public Token get(final int index) {
        return tokens.get(index);
    }

    public void set(final int index, final Token token) {
        tokens.set(index, token);
    }

    public void insert(final Token token) {
        tokens.add(token);
    }

    public void remove(final Token token) {
        tokens.remove(token);
    }

    public void clear() {
        tokens.clear();
    }

    public boolean isEmpty() {
        return !tokens.isEmpty();
    }

    public Player getOwner() {
        if (isEmpty()) {
            return tokens.get(0).getOwner();
        }

        return null;
    }

}
