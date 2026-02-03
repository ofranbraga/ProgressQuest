package com.progressquest.engine;

import com.progressquest.data.GameData;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import com.progressquest.model.Potion;
import com.progressquest.model.Quest;
import com.progressquest.util.RandomNameGenerator;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GameEngine {
    private final Character hero;
    private final Random rand = new Random();
    private ScheduledExecutorService scheduler;

    private Consumer<String> onLogUpdate;
    private Runnable onStatsUpdate;

    private Monster currentMonster;
    private GameState currentState;

    private int actionProgress = 0;
    private int actionMax = 10;
    private String currentAction = "Starting...";

    // --- Combat-only utilities ---
    // Null means no queued potion.
    private volatile Potion.Kind pendingPotion = null;

    // Regen tuning
    public static final double TURN_REGEN_PCT = 0.02;   // 2% per combat turn
    public static final double POST_COMBAT_REGEN_PCT = 0.20; // 20% after a fight ends

    public GameEngine(Character hero) {
        this.hero = hero;
        this.currentState = new CombatState();
    }

    public void setCallbacks(Consumer<String> onLogUpdate, Runnable onStatsUpdate) {
        this.onLogUpdate = onLogUpdate;
        this.onStatsUpdate = onStatsUpdate;
    }

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::tick, 0, 200, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }

    private void tick() {
        try {
            if (onStatsUpdate != null) onStatsUpdate.run();
            // Map-driven state switch: safe zone disables combat.
            if (GameData.SAFE_ZONE.equals(hero.getCurrentMap())) {
                if (!(currentState instanceof SafeZoneState)) {
                    currentState = new SafeZoneState();
                    actionProgress = 0;
                }
            } else {
                if (currentState == null || currentState instanceof SafeZoneState) {
                    currentState = new CombatState();
                    actionProgress = 0;
                }
            }
            currentState.update(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Called by the UI: consume a potion on the next combat turn. */
    public void requestPotionUse(Potion.Kind kind) {
        this.pendingPotion = kind;
    }

    /**
     * Used by CombatState to apply a queued potion (if any) at the start of a turn.
     * Returns true if a potion was consumed.
     */
    boolean tryConsumePendingPotion() {
        Potion.Kind kind = this.pendingPotion;
        if (kind == null) return false;
        // Clear request immediately so it doesn't repeat.
        this.pendingPotion = null;

        for (int i = 0; i < hero.getInventory().size(); i++) {
            Item it = hero.getInventory().get(i);
            if (it instanceof Potion p && p.getKind() == kind) {
                hero.getInventory().remove(i);
                if (kind == Potion.Kind.HEALTH) {
                    hero.healPercent(p.getRestorePercent());
                    log("You drink a health potion (+" + (int)Math.round(p.getRestorePercent() * 100) + "% HP)." );
                } else {
                    hero.regenManaPercent(p.getRestorePercent());
                    log("You drink a mana potion (+" + (int)Math.round(p.getRestorePercent() * 100) + "% MP)." );
                }
                return true;
            }
        }

        log("No potion of that type in your inventory.");
        return false;
    }

    void spawnMonster() {
        String mobName = GameData.getMonsterFromMap(hero.getCurrentMap());
        if (mobName == null || mobName.isBlank()) {
            // Safe-zone or misconfigured map.
            currentMonster = null;
            return;
        }
        int mobLevel = Math.max(1, hero.getLevel() + (rand.nextInt(3) - 1));
        currentMonster = new Monster(mobName, mobLevel);
        log("You encountered a " + mobName + " (Lvl " + mobLevel + ")!");
    }

    void handleMonsterDeath() {
        log("You killed " + currentMonster.getName() + "!");
        int levelBefore = hero.getLevel();
        hero.gainExperience(currentMonster.getRewardXP());
        int levelAfter = hero.getLevel();

        long gold = currentMonster.getGoldReward();
        if (gold > 0) {
            hero.addGold(gold);
            log("You found " + gold + " gold on the corpse.");
        }

        checkQuestProgress(currentMonster.getName());

        if (rand.nextDouble() < 0.3) {
            Item item = generateItem();
            hero.lootItem(item);
            log("Looted: " + item.getName());
        }

        // After-combat regeneration (limited): restore 20% of max HP/MP.
        hero.healPercent(POST_COMBAT_REGEN_PCT);
        hero.regenManaPercent(POST_COMBAT_REGEN_PCT);
        log("You catch your breath (+" + (int)Math.round(POST_COMBAT_REGEN_PCT * 100) + "% HP/MP)." );

        currentMonster = null;

        if (levelAfter > levelBefore) {
            setCurrentState(new LevelUpState(levelBefore, levelAfter));
        }
    }

    void checkQuestProgress(String killedMonsterName) {
        if (hero.getCurrentQuest() == null) {
            String target = GameData.generateQuestTarget();
            int amount = 3 + rand.nextInt(3);
            long rewardGold = 10L * hero.getLevel() + rand.nextInt(21);
            Quest q = new Quest(
                    GameData.generateQuestTitle(target),
                    "Hunt them down",
                    amount,
                    100 * hero.getLevel(),
                    null,
                    rewardGold
            );
            hero.setCurrentQuest(q);
            log("ACCEPTED QUEST: " + q.getTitle() + ". Find them!");
            return;
        }

        if (hero.getCurrentQuest().getTitle().contains(killedMonsterName)) {
            hero.getCurrentQuest().registerKill();
            log("Quest Progress: " + hero.getCurrentQuest().getCurrentKills() + "/" +
                    hero.getCurrentQuest().getRequiredKills());

            if (hero.getCurrentQuest().isCompleted()) {
                log("QUEST COMPLETED!");
                int levelBefore = hero.getLevel();
                hero.gainExperience(hero.getCurrentQuest().getRewardXP());
                int levelAfter = hero.getLevel();

                long qGold = hero.getCurrentQuest().getRewardGold();
                if (qGold > 0) {
                    hero.addGold(qGold);
                    log("You received " + qGold + " gold as quest reward.");
                }

                hero.setCurrentQuest(null);

                if (levelAfter > levelBefore) {
                    setCurrentState(new LevelUpState(levelBefore, levelAfter));
                }
            }
        }
    }

    Item generateItem() {
        // Exclude consumable slot from random equipment drops.
        Item.Slot[] slots = java.util.Arrays.stream(Item.Slot.values())
                .filter(s -> s != Item.Slot.POTION)
                .toArray(Item.Slot[]::new);
        Item.Slot slot = slots[rand.nextInt(slots.length)];
        String name = RandomNameGenerator.randomItemName();
        int bonus = 1 + (hero.getLevel() / 2) + rand.nextInt(3);
        return new Item(name, slot, bonus, "STR");
    }

    void log(String msg) {
        if (onLogUpdate != null) onLogUpdate.accept(msg);
    }

    // Helpers (mantidos sem quebrar chamadas existentes)
    Character getHero() { return hero; }
    Random getRand() { return rand; }

    void setCurrentState(GameState newState) {
        this.currentState = newState;
        this.actionProgress = 0;
    }

    public void setResting() { setCurrentState(new RestState()); }
    public void setFighting() { setCurrentState(new CombatState()); }

    void setCurrentAction(String action) { this.currentAction = action; }
    void setActionMax(int max) { this.actionMax = max; }
    void incrementActionProgress() { this.actionProgress++; }
    void resetActionProgress() { this.actionProgress = 0; }

    public int getActionProgress() { return actionProgress; }
    public int getActionMax() { return actionMax; }
    public String getCurrentAction() { return currentAction; }
    public Monster getCurrentMonster() { return currentMonster; }

    Monster _getCurrentMonsterInternal() { return currentMonster; }
    void _setCurrentMonsterInternal(Monster m) { this.currentMonster = m; }
}
