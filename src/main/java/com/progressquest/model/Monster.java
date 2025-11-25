package com.progressquest.model;

public class Monster {
    private final String name;
    private final int level;
    private final long rewardXP;
    private final double dropChance;

    public Monster(String name, int level, long rewardXP, double dropChance) {
        this.name = name;
        this.level = level;
        this.rewardXP = rewardXP;
        this.dropChance = dropChance;
    }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public long getRewardXP() { return rewardXP; }
    public double getDropChance() { return dropChance; }
}