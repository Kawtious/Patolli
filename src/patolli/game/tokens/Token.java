/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.tokens;

import patolli.game.Player;

public class Token {

    private final Player owner;

    private final int index;

    private final int initialPos;

    private int currentPos;

    public Token(final Player owner, final int index, final int initialPos) {
        this.owner = owner;
        this.index = index < 0 ? 0 : index;
        this.initialPos = initialPos;
        this.currentPos = initialPos;
    }

    public void advancePos(final int advance) {
        currentPos += advance;
    }

    public void markAsFinished() {
        currentPos = -2;
    }

    public Player getOwner() {
        return owner;
    }

    public int getIndex() {
        return index;
    }

    public int getInitialPos() {
        return initialPos;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(final int currentPos) {
        this.currentPos = currentPos;
    }

}
