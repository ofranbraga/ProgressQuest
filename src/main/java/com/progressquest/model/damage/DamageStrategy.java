package com.progressquest.model.damage;

import com.progressquest.model.Character;

/**
 * Strategy interface for computing the player's outgoing damage.
 *
 * <p>Implementations may optionally consume mana (e.g., magic attacks) and
 * provide a label for log/UI.</p>
 */
public interface DamageStrategy {

    /** @return the damage dealt for one attack. */
    int calculate(Character character);

    /** @return mana cost for one attack; default is 0 (no mana required). */
    default int manaCost(Character character) { return 0; }

    /** @return short label used in logs/UI (e.g., "Physical", "Magic"). */
    default String typeLabel() { return "Physical"; }

    /** @return verb used in the combat log (e.g., "strike", "cast"). */
    default String logVerb() { return "hit"; }
}
