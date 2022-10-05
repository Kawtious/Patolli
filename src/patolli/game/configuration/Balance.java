/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patolli.game.configuration;

import patolli.game.Player;

public class Balance {

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
        this.balance += balance;
    }

    public void give(final int balance) {
        this.balance -= balance;
    }

    @Override
    public String toString() {
        return String.valueOf(balance);
    }

}
