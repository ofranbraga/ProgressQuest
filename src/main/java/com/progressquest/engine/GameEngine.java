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
    private boolean isResting = false;

    private int actionProgress = 0;
    private int actionMax = 10;
    private String currentAction = "Starting...";

    public GameEngine(Character hero) {
        this.hero = hero;
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

            //maquina de estados (descansando ou em batalha?)
            if (hero.isDead() || isResting) {
                performRest();
            } else {
                performCombatLoop();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performRest() {
        currentAction = "Resting (Regenerating HP/MP)...";
        isResting = true;
        actionMax = 5;
        actionProgress++;

        if (actionProgress >= actionMax) {
            hero.heal(hero.getHpMax() / 10);
            hero.regenMana(hero.getMpMax() / 10);
            actionProgress = 0;

            if (hero.getHpCurrent() >= hero.getHpMax()) {
                isResting = false;
                log("You are fully rested and ready to fight!");
                currentMonster = null;
            }
        }
    }

    private void performCombatLoop() {
        if (currentMonster == null) {
            spawnMonster();
            actionProgress = 0;
            return;
        }

        currentAction = "Fighting " + currentMonster.getName();
        actionMax = 10; //velocidade do turno
        actionProgress++;

        if (actionProgress >= actionMax) {
            actionProgress = 0;

            //1 player ataca Monstro
            int playerDmg = hero.calculateDamage();
            if (rand.nextInt(20) == 0) playerDmg *= 2; // Crítico

            currentMonster.takeDamage(playerDmg);
            log("You hit " + currentMonster.getName() + " for " + playerDmg + " damage.");

            //2 monstro morreu?
            if (currentMonster.isDead()) {
                handleMonsterDeath();
                return;
            }

            //3 monstro ataca player
            int monsterDmg = currentMonster.getDamage();
            //redução de dano simples pela armadura
            Item armor = hero.getEquipment().get(Item.Slot.HAUBERK);
            if (armor != null) monsterDmg -= (armor.getBonus() / 2);
            if (monsterDmg < 1) monsterDmg = 1;

            hero.takeDamage(monsterDmg);
            log(currentMonster.getName() + " hits you for " + monsterDmg + " damage!");

            //player morreu?
            if (hero.isDead()) {
                log("You were defeated by " + currentMonster.getName() + "!");
                isResting = true;
            }
        }
    }

    private void spawnMonster() {
        //spawna monstro baseado no mapa atual do jogador
        String mobName = GameData.getMonsterFromMap(hero.getCurrentMap());
        int mobLevel = Math.max(1, hero.getLevel() + (rand.nextInt(3) - 1));
        currentMonster = new Monster(mobName, mobLevel);
        log("You encountered a " + mobName + " (Lvl " + mobLevel + ")!");
    }

    private void handleMonsterDeath() {
        log("You killed " + currentMonster.getName() + "!");
        hero.gainExperience(currentMonster.getRewardXP());

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
            Quest q = new Quest(GameData.generateQuestTitle(target), "Hunt them down", amount, 100 * hero.getLevel(), null);
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

    private void log(String msg) {
        if (onLogUpdate != null) onLogUpdate.accept(msg);
    }

    public int getActionProgress() { return actionProgress; }
    public int getActionMax() { return actionMax; }
    public String getCurrentAction() { return currentAction; }
    public Monster getCurrentMonster() { return currentMonster; }
}