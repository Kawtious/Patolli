/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.spaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import patolli.game.Player;
import patolli.game.Token;

public abstract class Space {

    private final List<Token> tokens = new ArrayList<>();

    public Token getToken(final int index) {
        return tokens.get(index);
    }

    public void setToken(final int index, final Token token) {
        tokens.set(index, token);
    }

    public void insertToken(final Token token) {
        tokens.add(token);
    }

    public void removeToken(final Token token) {
        tokens.remove(token);
    }

    public void clearTokens() {
        tokens.clear();
    }

    public boolean hasNoTokens() {
        return !tokens.isEmpty();
    }

    public Player getOwner() {
        if (hasNoTokens()) {
            return tokens.get(0).getOwner();
        }

        return null;
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

}
