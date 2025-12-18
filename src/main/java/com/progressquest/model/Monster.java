package com.progressquest.model;

public class Monster {
    private final String name;
    private final int level;
    private final long rewardXP;

    //atributos de combate
    private int maxHp;
    private int currentHp;
    private int damage;

    public Monster(String name, int level) {
        this.name = name;
        this.level = level;
        this.rewardXP = 20L * level;

        //HP aumenta com nível
        this.maxHp = 15 + (level * 5);
        this.currentHp = this.maxHp;
        this.damage = 2 + (level * 2);
    }

    public void takeDamage(int dmg) {
        this.currentHp -= dmg;
        if (this.currentHp < 0) this.currentHp = 0;
    }

    public boolean isDead() { return currentHp <= 0; }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public long getRewardXP() { return rewardXP; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getDamage() { return damage; }
}