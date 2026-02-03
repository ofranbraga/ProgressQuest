package com.progressquest.model.damage;

import com.progressquest.model.Character;
import com.progressquest.model.Item;

/**
 * Magical damage based mainly on INT plus a smaller weapon focus bonus.
 *
 * <p>Magic attacks consume mana. If the character cannot pay the mana cost,
 * the engine will fall back to a weak physical hit for that turn.</p>
 */
public class MagicDamage implements DamageStrategy {
    @Override
    public int calculate(Character character) {
        int dmg = 1 + (character.getAttributes().get("INT") / 2);

        Item weapon = character.getEquipment().get(Item.Slot.WEAPON);
        if (weapon != null) dmg += Math.max(1, weapon.getBonus() / 2);

        return dmg;
    }

    @Override
    public int manaCost(Character character) {
        // Scales gently with INT and level; minimum 2 MP per cast.
        int intel = character.getAttributes().get("INT");
        int lvl = character.getLevel();
        return Math.max(2, 2 + (intel / 10) + (lvl / 5));
    }

    @Override
    public String typeLabel() { return "Magic"; }

    @Override
    public String logVerb() { return "cast"; }
}
