package com.progressquest.model;

public class PhysicalDamage implements DamageStrategy {
    @Override
    public int calculateDamage(Character character) {
        int dmg = 2 + (character.getAttributes().get("STR") / 2);
        dmg += character.getWeaponBonus();
        return dmg;
    }
}
