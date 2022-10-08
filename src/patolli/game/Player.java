/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Player {

    private final UUID id;

    private Balance balance;

    private Dice dice;

    private String name;

    private Color color;

    private final ArrayList<Token> tokens = new ArrayList<>();

    private int currentToken = 0;

    public Player() {
        this.id = UUID.randomUUID();
        this.name = "DEFAULT";
        this.color = Color.GRAY;
        this.balance = new Balance();
        this.dice = new Dice();
    }

    public Player(final String name, final Color color) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.color = color;
        this.balance = new Balance();
        this.dice = new Dice();
    }

    public Player(final String name, final Color color, final Balance balance) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.color = color;
        this.balance = balance;
        this.dice = new Dice();
    }

    public Player(final String name, final Color color, final Balance balance, final Dice dice) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.color = color;
        this.balance = balance;
        this.dice = dice;
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
        currentToken = 0;
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
                    return;
                }
            }
        } else {
            for (int i = currentToken + 1; i < countTokens(); i++) {
                if (tokenIsInPlay(getToken(i))) {
                    currentToken = i;
                    return;
                }
            }

            for (int j = 0; j < currentToken; j++) {
                if (tokenIsInPlay(getToken(j))) {
                    currentToken = j;
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

    public Dice getDice() {
        return dice;
    }

    public void setDice(final Dice dice) {
        this.dice = dice;
    }

    @Override
    public String toString() {
        return name;
    }

    public static class Balance {

        private final int DEFAULT_BALANCE = 100;

        private int balance;

        public Balance() {
            this.balance = DEFAULT_BALANCE;
        }

        public Balance(final int balance) {
            this.balance = balance;
        }

        public int get() {
            return balance;
        }

        public void set(final int balance) {
            this.balance = balance;
        }

        public boolean isBroke() {
            return balance <= 0;
        }

        public int compare(final Player player) {
            return balance - player.getBalance().get();
        }

        public void take(final int balance) {
            this.balance -= balance;
        }

        public void give(final int balance) {
            this.balance += balance;
        }

        @Override
        public String toString() {
            return String.valueOf(balance);
        }

    }

    public static class Dice {

        private int result;
        private int outcome;

        public Dice() {
            throwDice();
        }

        public int nextResult() {
            throwDice();
            return result;
        }

        public int nextOutcome() {
            throwDice();
            return outcome;
        }

        private void throwDice() {
            // nextInt is normally exclusive of the top value,
            // so add 1 to make it inclusive
            result = ThreadLocalRandom.current().nextInt(0, 5 + 1);
            determineOutcome();
        }

        private void determineOutcome() {
            outcome = result == 5 ? 10 : result;
        }

        public int getResult() {
            return result;
        }

        public void setResult(final int result) {
            this.result = result;
            determineOutcome();
        }

        public int getOutcome() {
            return outcome;
        }

        @Override
        public String toString() {
            return String.valueOf(result);
        }

    }

}
