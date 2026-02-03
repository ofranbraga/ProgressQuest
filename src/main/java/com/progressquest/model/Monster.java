package com.progressquest.model;

import java.util.Random;

/**
 * A combat opponent in Idle mode.
 *
 * <p>2D spawn and pathing logic were removed.</p>
 */
public class Monster extends Entity {
    private final long rewardXP;
    private int damage;
    private final long goldReward;

    public Monster(String name, int level) {
        super(name, level);
        this.rewardXP = 20L * level;

        this.hpMax = 15 + (level * 5);
        this.hpCurrent = this.hpMax;
        this.damage = 2 + (level * 2);

        this.goldReward = 5L * level + new Random().nextInt(6); // 0-5 extra
    }

    public long getRewardXP() { return rewardXP; }

    // Mantidos por compatibilidade com UI (IdleMode)
    public int getMaxHp() { return hpMax; }
    public int getCurrentHp() { return hpCurrent; }

    public int getDamage() { return damage; }
    public long getGoldReward() { return goldReward; }
}
