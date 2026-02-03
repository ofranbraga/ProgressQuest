package com.progressquest.engine;

/**
 * Transitional state shown right after a level up.
 *
 * <p>Leveling logic remains inside Character. This state exists to emit log
 * and briefly pause combat so the player perceives the event.</p>
 */
public final class LevelUpState implements GameState {
    private final int fromLevel;
    private final int toLevel;
    private boolean logged = false;

    public LevelUpState(int fromLevel, int toLevel) {
        this.fromLevel = fromLevel;
        this.toLevel = toLevel;
    }

    @Override
    public void update(GameEngine ctx) {
        ctx.setCurrentAction("Level Up!");
        ctx.setActionMax(6);
        ctx.incrementActionProgress();

        if (!logged) {
            logged = true;
            ctx.log("LEVEL UP! " + ctx.getHero().getName() +
                    " advanced from " + fromLevel + " to " + toLevel + ".");
            ctx.log("You gained attribute points. Open Status to spend them.");
        }

        if (ctx.getActionProgress() >= ctx.getActionMax()) {
            ctx.resetActionProgress();
            ctx.setCurrentState(new CombatState());
        }
    }
}
