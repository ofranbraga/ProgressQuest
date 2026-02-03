package com.progressquest.engine;

import com.progressquest.data.GameData;

/**
 * Safe zone: no monsters spawn and the player can shop.
 *
 * <p>The UI is responsible for opening the shop window; the engine simply
 * keeps combat disabled while the character is in the safe-zone map.</p>
 */
public class SafeZoneState implements GameState {
    @Override
    public void update(GameEngine ctx) {
        // If the player left the town, resume combat loop.
        if (!GameData.SAFE_ZONE.equals(ctx.getHero().getCurrentMap())) {
            ctx.setCurrentState(new CombatState());
            return;
        }

        ctx._setCurrentMonsterInternal(null);
        ctx.setCurrentAction("In " + GameData.SAFE_ZONE + " (Safe Zone)");
        ctx.setActionMax(10);
        // idle progress bar animation, purely cosmetic
        ctx.incrementActionProgress();
        if (ctx.getActionProgress() >= ctx.getActionMax()) {
            ctx.resetActionProgress();
        }
    }
}
