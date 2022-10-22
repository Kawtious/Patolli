/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

public class Token {

    private final Player owner;

    private final int index;

    private final int initialPos;

    private int position;

    public Token(final Player owner, final int index, final int position) {
        this.owner = owner;
        this.index = (index < 0) ? 0 : index;
        this.initialPos = position;
        this.position = position;
    }

    public void advancePos(final int advance) {
        position += advance;
    }

    public void markAsDestroyed() {
        position = -1;
    }

    public void markAsFinished() {
        position = -2;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(final int position) {
        this.position = position;
    }

}
