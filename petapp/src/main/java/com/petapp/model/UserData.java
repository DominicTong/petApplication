package com.petapp.model;

/**
 * Snapshot of the player's persistent stats.
 */
public class UserData {

    private final int coins;
    private final int totalTasksCompleted;

    public UserData(int coins, int totalTasksCompleted) {
        this.coins               = coins;
        this.totalTasksCompleted = totalTasksCompleted;
    }

    public int getCoins()               { return coins; }
    public int getTotalTasksCompleted() { return totalTasksCompleted; }
}
