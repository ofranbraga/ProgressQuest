package com.progressquest.engine;

public class RestState implements GameState {
    @Override
    public void update(GameEngine context) {
        context.setCurrentAction("Resting (Regenerating HP/MP)...");
        context.setActionMax(5);
        context.incrementActionProgress();

        if (context.getActionProgress() < context.getActionMax()) {
            return;
        }

        context.resetActionProgress();
        context.getHero().heal(context.getHero().getHpMax() / 10);
        context.getHero().regenMana(context.getHero().getMpMax() / 10);

        if (context.getHero().getHpCurrent() >= context.getHero().getHpMax()) {
            context.log("You are fully rested and ready to fight!");
            context.setCurrentMonster(null);
            context.setState(new CombatState());
        }
    }
}
