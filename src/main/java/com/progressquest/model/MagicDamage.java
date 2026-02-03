package com.progressquest.model;

public class MagicDamage implements DamageStrategy {
    @Override
    public int calculateDamage(Character character) {
        int dmg = 2 + (character.getAttributes().get("INT") / 2);
        dmg += character.getWeaponBonus();
        return dmg;
    }
}
