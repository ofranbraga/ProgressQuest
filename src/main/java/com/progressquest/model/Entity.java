package com.progressquest.model;

public abstract class Entity {
    protected String name;
    protected int level;

    protected int hpMax;
    protected int hpCurrent;

    protected Entity() {
    }

    protected Entity(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getHpMax() {
        return hpMax;
    }

    public int getHpCurrent() {
        return hpCurrent;
    }

    /** Apply incoming damage to this entity. */
    public void takeDamage(int dmg) {
        this.hpCurrent -= Math.max(0, dmg);
        if (this.hpCurrent < 0) this.hpCurrent = 0;
    }

    /** @return true if entity has no remaining HP. */
    public boolean isDead() {
        return hpCurrent <= 0;
    }
}
