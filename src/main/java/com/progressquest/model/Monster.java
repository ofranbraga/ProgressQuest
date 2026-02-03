package com.progressquest.model;

import java.util.Random;

public class Monster extends Entity {
    private final long rewardXP;

    private int damage;

    // recompensa em ouro
    private final long goldReward;

    public Monster(String name, int level) {
        this.name = name;
        this.level = level;
        this.rewardXP = 20L * level;

        this.hpMax = 15 + (level * 5);
        this.hpCurrent = this.hpMax;
        this.damage = 2 + (level * 2);

        //posição aleatória inicial (vai ser sobrescrita pelo spawn)
        this.x = 0;
        this.y = 0;

        // gold reward baseado no level com pequena variação
        this.goldReward = 5L * level + new Random().nextInt(6); // 0-5 extra
    }

    //spawn aleatorio de monstros
    public void spawnRandomly(int screenWidth, int screenHeight) {
        Random r = new Random();
        this.x = r.nextInt(screenWidth - 50);
        this.y = r.nextInt(screenHeight - 50);
    }

    public long getRewardXP() { return rewardXP; }
    public int getMaxHp() { return hpMax; }
    public int getCurrentHp() { return hpCurrent; }
    public int getDamage() { return damage; }

    public long getGoldReward() { return goldReward; }

    //ia bem basiquinha de monstros para q eles vao ate o player
    public void moveTowards(double targetX, double targetY, double speed) {
        double dx = targetX - x;
        double dy = targetY - y;
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return;
        double ndx = dx / len;
        double ndy = dy / len;
        double adjust = 1.7;
        this.x += ndx * speed * adjust;
        this.y += ndy * speed * adjust;
    }
}
