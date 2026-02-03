package com.progressquest.model.damage;

import com.progressquest.model.Character;
import com.progressquest.model.Item;

/** Physical damage based mainly on STR and weapon bonus. */
public class PhysicalDamage implements DamageStrategy {
    @Override
    public int calculate(Character character) {
        int dmg = 2 + (character.getAttributes().get("STR") / 2);
        Item weapon = character.getEquipment().get(Item.Slot.WEAPON);
        if (weapon != null) dmg += weapon.getBonus();
        return dmg;
    }

    @Override
    public String typeLabel() { return "Physical"; }

    @Override
    public String logVerb() { return "strike"; }
}
