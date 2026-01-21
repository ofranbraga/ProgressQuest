package com.progressquest.model;

//classe que representa a fireball
public class Projectile {
    public double x, y, dx, dy;
    public boolean active = true;

    public Projectile(double x, double y, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }
}