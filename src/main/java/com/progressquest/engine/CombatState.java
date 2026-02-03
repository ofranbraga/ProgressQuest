package com.progressquest.engine;

import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import com.progressquest.model.damage.DamageStrategy;
import com.progressquest.model.damage.PhysicalDamage;

/** Combat loop: spawns monsters and runs turn-based exchanges. */
public class CombatState implements GameState {
    @Override
    public void update(GameEngine ctx) {
        if (ctx.getHero().isDead()) {
            ctx.setCurrentState(new RestState());
            return;
        }

        Monster currentMonster = ctx._getCurrentMonsterInternal();
        if (currentMonster == null) {
            ctx.spawnMonster();
            ctx.resetActionProgress();
            return;
        }

        ctx.setCurrentAction("Fighting " + currentMonster.getName());
        ctx.setActionMax(10);
        ctx.incrementActionProgress();

        if (ctx.getActionProgress() < ctx.getActionMax()) return;
        ctx.resetActionProgress();

        // --- Start of a combat turn ---
        // If the player queued a potion via UI, consume it now.
        ctx.tryConsumePendingPotion();

        var hero = ctx.getHero();
        var rand = ctx.getRand();

        DamageStrategy strat = hero.getDamageStrategy();
        int manaCost = (strat != null) ? strat.manaCost(hero) : 0;

        int playerDmg;
        String verb;
        String typeLabel;

        if (manaCost > 0) {
            // Magic attack: pay mana, otherwise fall back to a weak physical hit.
            if (!hero.spendMana(manaCost)) {
                typeLabel = "Fizzle";
                verb = "swing";
                playerDmg = Math.max(1, new PhysicalDamage().calculate(hero) / 2);
                ctx.log("Not enough MP to cast! You " + verb + " at " + currentMonster.getName() + " in desperation.");
            } else {
                typeLabel = (strat != null) ? strat.typeLabel() : "Magic";
                verb = (strat != null) ? strat.logVerb() : "cast";
                playerDmg = hero.calculateDamage();
                ctx.log("You " + verb + " a spell at " + currentMonster.getName() + " (cost " + manaCost + " MP).");
            }
        } else {
            typeLabel = (strat != null) ? strat.typeLabel() : "Physical";
            verb = (strat != null) ? strat.logVerb() : "strike";
            playerDmg = hero.calculateDamage();
        }

        if (rand.nextInt(20) == 0) {
            playerDmg *= 2;
            ctx.log("Critical " + typeLabel + " hit!");
        }

        currentMonster.takeDamage(playerDmg);
        ctx.log("You " + verb + " " + currentMonster.getName() + " for " + playerDmg + " damage.");

        // Occasional bonus magic damage proc (works for any class): "arcane surge".
        if (!currentMonster.isDead() && rand.nextInt(7) == 0) { // ~14%
            int surge = Math.max(1, (hero.getAttributes().get("INT") + hero.getAttributes().get("WIS")) / 2);
            surge += rand.nextInt(Math.max(2, hero.getLevel() + 1));
            currentMonster.takeDamage(surge);
            ctx.log("Arcane Surge! You deal an extra " + surge + " magic damage.");
        }

        if (currentMonster.isDead()) {
            ctx.handleMonsterDeath();
            return;
        }

        int monsterDmg = currentMonster.getDamage();
        Item armor = hero.getEquipment().get(Item.Slot.HAUBERK);
        if (armor != null) monsterDmg -= (armor.getBonus() / 2);
        if (monsterDmg < 1) monsterDmg = 1;

        // Monster occasionally uses a magic attack that ignores armor.
        if (rand.nextInt(8) == 0) { // ~12.5%
            int magic = Math.max(1, currentMonster.getDamage() / 2 + rand.nextInt(1 + currentMonster.getLevel()));
            hero.takeDamage(magic);
            ctx.log(currentMonster.getName() + " casts a spell for " + magic + " magic damage!");
        } else {
            hero.takeDamage(monsterDmg);
            ctx.log(currentMonster.getName() + " hits you for " + monsterDmg + " damage!");
        }

        // End-of-turn regen (limited): only a small % each combat turn.
        hero.healPercent(GameEngine.TURN_REGEN_PCT);
        hero.regenManaPercent(GameEngine.TURN_REGEN_PCT);

        if (hero.isDead()) {
            ctx.log("You were defeated by " + currentMonster.getName() + "!");
            ctx.setCurrentState(new RestState());
        }
    }
}
