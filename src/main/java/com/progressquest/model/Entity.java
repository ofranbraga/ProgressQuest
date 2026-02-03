package com.progressquest.model;

public abstract class Entity {
    protected String name;
    protected int level;
    protected int hpMax;
    protected int hpCurrent;
    protected double x;
    protected double y;

    public void takeDamage(int dmg) {
        this.hpCurrent -= dmg;
        if (this.hpCurrent < 0) this.hpCurrent = 0;
    }

    public boolean isDead() {
        return hpCurrent <= 0;
    }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getHpMax() { return hpMax; }
    public int getHpCurrent() { return hpCurrent; }
    public double getX() { return x; }
    public double getY() { return y; }
}
