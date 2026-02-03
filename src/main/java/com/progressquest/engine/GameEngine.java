package com.progressquest.engine;

import com.progressquest.data.GameData;
import com.progressquest.model.*;
import com.progressquest.model.Character;
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

    //estados do jogo
    private Monster currentMonster;
    private GameState currentState;

    private int actionProgress = 0;
    private int actionMax = 10;
    private String currentAction = "Starting...";

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
        //loop a cada 200ms
        scheduler.scheduleAtFixedRate(this::tick, 0, 200, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }

    private void tick() {
        try {
            if (onStatsUpdate != null) onStatsUpdate.run();

            if (currentState != null) {
                currentState.update(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void spawnMonster() {
        //spawna monstro baseado no mapa atual do jogador
        String mobName = GameData.getMonsterFromMap(hero.getCurrentMap());
        int mobLevel = Math.max(1, hero.getLevel() + (rand.nextInt(3) - 1));
        currentMonster = new Monster(mobName, mobLevel);
        log("You encountered a " + mobName + " (Lvl " + mobLevel + ")!");
    }

    void handleMonsterDeath() {
        log("You killed " + currentMonster.getName() + "!");
        hero.gainExperience(currentMonster.getRewardXP());

        // dar gold ao jogador
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

        currentMonster = null;
    }

    private void checkQuestProgress(String killedMonsterName) {
        if (hero.getCurrentQuest() == null) {
            //nova quest
            String target = GameData.generateQuestTarget();
            int amount = 3 + rand.nextInt(3);
            long rewardGold = 10L * hero.getLevel() + rand.nextInt(21); // 0-20 random
            Quest q = new Quest(GameData.generateQuestTitle(target), "Hunt them down", amount, 100 * hero.getLevel(), null, rewardGold);
            hero.setCurrentQuest(q);
            log("ACCEPTED QUEST: " + q.getTitle() + ". Find them!");
        } else {
            //verifica se o monstro morto é o alvo da quest
            if (hero.getCurrentQuest().getTitle().contains(killedMonsterName)) {
                hero.getCurrentQuest().registerKill();
                log("Quest Progress: " + hero.getCurrentQuest().getCurrentKills() + "/" + hero.getCurrentQuest().getRequiredKills());

                if (hero.getCurrentQuest().isCompleted()) {
                    log("QUEST COMPLETED!");
                    hero.gainExperience(hero.getCurrentQuest().getRewardXP());
                    long qGold = hero.getCurrentQuest().getRewardGold();
                    if (qGold > 0) {
                        hero.addGold(qGold);
                        log("You received " + qGold + " gold as quest reward.");
                    }
                    hero.setCurrentQuest(null);
                }
            }
        }
    }

    private Item generateItem() {
        Item.Slot[] slots = Item.Slot.values();
        Item.Slot slot = slots[rand.nextInt(slots.length)];
        String name = RandomNameGenerator.randomItemName();
        int bonus = 1 + (hero.getLevel() / 2) + rand.nextInt(3);
        return new Item(name, slot, bonus, "STR");
    }

    void log(String msg) {
        if (onLogUpdate != null) onLogUpdate.accept(msg);
    }

    public int getActionProgress() { return actionProgress; }
    public int getActionMax() { return actionMax; }
    public String getCurrentAction() { return currentAction; }
    public Monster getCurrentMonster() { return currentMonster; }

    Character getHero() { return hero; }
    Random getRand() { return rand; }
    void setState(GameState state) { this.currentState = state; }
    void setCurrentAction(String action) { this.currentAction = action; }
    void setActionMax(int actionMax) { this.actionMax = actionMax; }
    void incrementActionProgress() { this.actionProgress++; }
    void resetActionProgress() { this.actionProgress = 0; }
    void setCurrentMonster(Monster monster) { this.currentMonster = monster; }
}
