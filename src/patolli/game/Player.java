/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;
import patolli.game.configuration.Balance;
import patolli.game.tokens.Token;
import patolli.game.utils.Console;

public class Player {

    private final UUID id;

    private String name;

    private Color color;

    private final ArrayList<Token> tokens = new ArrayList<>();

    private int currentToken = 0;

    private Balance balance;

    public Player(final String name, final Color color, final Balance balance) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.color = color;
        this.balance = balance;
    }

    public Token getCurrentToken() {
        if (!tokenIsInPlay(getToken(currentToken))) {
            selectNextToken();
        }

        return getToken(currentToken);
    }

    public Token createToken(final int initialPos) {
        final Token token = new Token(this, countTokens(), initialPos);

        tokens.add(token);

        return token;
    }

    public Token getToken(final int index) {
        return tokens.get(index);
    }

    public int countTokens() {
        return tokens.size();
    }

    public int tokensInPlay() {
        if (tokens.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Token token : tokens) {
            if (tokenIsInPlay(token)) {
                count++;
            }
        }

        return count;
    }

    public boolean tokenIsInPlay(final Token token) {
        return token.getCurrentPos() >= 0;
    }

    public boolean hasTokens() {
        if (tokens.isEmpty()) {
            return false;
        }

        return countTokens() > 0;
    }

    public int finishedTokens() {
        int count = 0;

        for (Token token : tokens) {
            if (token.getCurrentPos() == -2) {
                count++;
            }
        }

        return count;
    }

    public void clearTokens() {
        tokens.clear();
    }

    public void selectNextToken() {
        if (tokens.isEmpty()) {
            return;
        }

        if (currentToken == 0) {
            for (int i = 1; i < countTokens(); i++) {
                if (tokenIsInPlay(getToken(i))) {
                    currentToken = i;

                    Console.WriteLine("Player", "Selecting token " + currentToken + " of player " + name);
                    return;
                }
            }
        } else {
            for (int i = currentToken + 1; i < countTokens(); i++) {
                if (tokenIsInPlay(getToken(i))) {
                    currentToken = i;

                    Console.WriteLine("Player", "Selecting token " + currentToken + " of player " + name);
                    return;
                }
            }

            for (int j = 0; j < currentToken; j++) {
                if (tokenIsInPlay(getToken(j))) {
                    currentToken = j;

                    Console.WriteLine("Player", "Selecting token " + currentToken + " of player " + name);
                    return;
                }
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(final Color color) {
        this.color = color;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public Balance getBalance() {
        return balance;
    }

    public void setBalance(final Balance balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return name;
    }

}
