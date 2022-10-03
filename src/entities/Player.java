/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;
import utilities.Console;

public class Player {

    private final UUID id;

    private String name;

    private Color color;

    private final ArrayList<Token> tokens = new ArrayList<>();

    private final int maxTokens;

    private int currentToken = 1;

    private int balance;

    public Player(final String name, final Color color, final int balance, final int maxTokens) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.color = color;
        this.balance = balance;
        this.maxTokens = maxTokens;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getCurrentToken() {
        if (!tokenIsInPlay(getToken(currentToken))) {
            selectNextToken();
        }

        return currentToken;
    }

    public String getIdString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("(").append(id).append(")");
        return sb.toString();
    }

    public boolean isBroke() {
        return balance <= 0;
    }

    public Token createAndAssignToken(final int initialPos) {
        if (getTokensCount() < maxTokens) {
            final Token token = new Token(this, initialPos);

            tokens.add(token);

            return token;
        }

        return null;
    }

    public Token getToken(final int index) {
        return tokens.get(index - 1);
    }

    public int getTokenIndex(final Token token) {
        if (!tokens.isEmpty()) {
            for (int i = 1; i <= getTokensCount(); i++) {
                if (getToken(i).equals(token)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public Token getFirstToken() {
        if (!tokens.isEmpty()) {
            return getToken(1);
        }

        return null;
    }

    public Token getLastToken() {
        if (!tokens.isEmpty()) {
            return getToken(getTokensCount());
        }

        return null;
    }

    public int getTokensCount() {
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
        return token.getCurrentPos() >= 1;
    }

    public boolean hasTokens() {
        if (tokens.isEmpty()) {
            return false;
        }

        return getTokensCount() > 0;
    }

    public boolean hasInsertedAllTokens() {
        return getTokensCount() >= maxTokens;
    }

    public int countFinishedTokens() {
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

        if (tokensInPlay() > 1) {
            if (currentToken > 1) {
                for (int i = currentToken + 1; i <= getTokensCount(); i++) {
                    if (tokenIsInPlay(getToken(i))) {
                        currentToken = i;
                        return;
                    }
                }

                for (int j = 1; j <= currentToken; j++) {
                    if (tokenIsInPlay(getToken(j))) {
                        currentToken = j;
                        return;
                    }
                }
            } else {
                for (int i = 2; i <= getTokensCount(); i++) {
                    if (tokenIsInPlay(getToken(i))) {
                        currentToken = i;
                        return;
                    }
                }
            }
        } else {
            for (int i = 1; i <= getTokensCount(); i++) {
                if (tokenIsInPlay(getToken(i))) {
                    currentToken = i;
                    return;
                }
            }
        }

        Console.WriteLine("Selecting token " + currentToken + " of player " + getName());
    }

}
