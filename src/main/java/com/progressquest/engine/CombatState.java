package com.progressquest.engine;

import com.progressquest.model.Item;
import com.progressquest.model.Monster;

public class CombatState implements GameState {
    @Override
    public void update(GameEngine context) {
        if (context.getHero().isDead()) {
            context.setState(new RestState());
            return;
        }

        if (context.getCurrentMonster() == null) {
            context.spawnMonster();
            context.resetActionProgress();
            return;
        }

        Monster currentMonster = context.getCurrentMonster();
        context.setCurrentAction("Fighting " + currentMonster.getName());
        context.setActionMax(10);
        context.incrementActionProgress();

        if (context.getActionProgress() < context.getActionMax()) {
            return;
        }

        context.resetActionProgress();

        int playerDmg = context.getHero().calculateDamage();
        if (context.getRand().nextInt(20) == 0) playerDmg *= 2;

        currentMonster.takeDamage(playerDmg);
        context.log("You hit " + currentMonster.getName() + " for " + playerDmg + " damage.");

        if (currentMonster.isDead()) {
            context.handleMonsterDeath();
            return;
        }

        int monsterDmg = currentMonster.getDamage();
        Item armor = context.getHero().getEquipment().get(Item.Slot.HAUBERK);
        if (armor != null) monsterDmg -= (armor.getBonus() / 2);
        if (monsterDmg < 1) monsterDmg = 1;

        context.getHero().takeDamage(monsterDmg);
        context.log(currentMonster.getName() + " hits you for " + monsterDmg + " damage!");

        if (context.getHero().isDead()) {
            context.log("You were defeated by " + currentMonster.getName() + "!");
            context.setState(new RestState());
        }
    }
}
