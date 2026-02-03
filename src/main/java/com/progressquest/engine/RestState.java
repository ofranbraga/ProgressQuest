package com.progressquest.engine;

/** Resting loop: recovery after being defeated (limited regeneration). */
public class RestState implements GameState {
    @Override
    public void update(GameEngine ctx) {
        ctx.setCurrentAction("Resting (Recovering after defeat)...");
        ctx.setActionMax(10);
        ctx.incrementActionProgress();

        if (ctx.getActionProgress() >= ctx.getActionMax()) {
            ctx.resetActionProgress();

            var hero = ctx.getHero();
            // Revive with limited resources (20% of max), then send the player to the safe zone.
            if (hero.isDead()) {
                hero.healPercent(GameEngine.POST_COMBAT_REGEN_PCT);
                hero.regenManaPercent(GameEngine.POST_COMBAT_REGEN_PCT);
            } else {
                // If we ever enter rest while alive, still only top up a bit.
                hero.healPercent(GameEngine.POST_COMBAT_REGEN_PCT);
                hero.regenManaPercent(GameEngine.POST_COMBAT_REGEN_PCT);
            }

            ctx.log("You recover (+20% HP/MP) and return to town.");
            hero.setCurrentMap(com.progressquest.data.GameData.SAFE_ZONE);
            ctx._setCurrentMonsterInternal(null);
            ctx.setCurrentState(new SafeZoneState());
        }
    }
}
