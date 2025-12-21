package com.progressquest.model;

import java.util.Random;

public class Monster {
    private final String name;
    private final int level;
    private final long rewardXP;

    private int maxHp;
    private int currentHp;
    private int damage;

    private double x, y;

    public Monster(String name, int level) {
        this.name = name;
        this.level = level;
        this.rewardXP = 20L * level;

        this.maxHp = 15 + (level * 5);
        this.currentHp = this.maxHp;
        this.damage = 2 + (level * 2);

        //posição aleatória inicial (vai ser sobrescrita pelo spawn)
        this.x = 0;
        this.y = 0;
    }

    //spawn aleatorio de monstros
    public void spawnRandomly(int screenWidth, int screenHeight) {
        Random r = new Random();
        this.x = r.nextInt(screenWidth - 50);
        this.y = r.nextInt(screenHeight - 50);
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

    public double getX() { return x; }
    public double getY() { return y; }

    //ia bem basiquinha de monstros para q eles vao ate o player
    public void moveTowards(double targetX, double targetY, double speed) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        if (distance > 0) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }
}