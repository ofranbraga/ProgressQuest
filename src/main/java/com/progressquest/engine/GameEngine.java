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
        scheduler.scheduleAtFixedRate(this::tick, 0, 200, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) scheduler.shutdown();
    }

    private int actionProgress = 0;
    private int actionMax = 10;
    private String currentAction = "Thinking...";

    private void tick(){
        try{
            actionProgress++;

            //atualiza a barra de progresso
            if (onStatsUpdate != null) onStatsUpdate.run();

            if (actionProgress >= actionMax) {
                completeAction();
                actionProgress = 0;
                actionMax = 5 + rand.nextInt(15);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void completeAction(){
        if (hero.getCurrentQuest() == null){
            Quest q = new Quest(GameData.generateQuestName(), "...", 3 + rand.nextInt(5), 100, null);
            hero.setCurrentQuest(q);
            log("Accepted quest: " + q.getTitle());
            currentAction = "Questing...";
        } else if (!hero.getCurrentQuest().isCompleted()){
            Monster m = new Monster(GameData.getRandomMonster(), hero.getLevel(), 50, 0.3);
            hero.getCurrentQuest().registerKill();
            hero.gainExperience(m.getRewardXP());
            log("Defeated...");

            //drop de item
            if (rand.nextDouble() < 0.3) {
                Item item = generateItem();
                hero.lootItem(item);
                log("Looted: " + item.getName());
            }
            currentAction = "Killing " + m.getName();
        }else {
            log("Completed quest: " + hero.getCurrentQuest().getTitle());
            hero.gainExperience(hero.getCurrentQuest().getRewardXP());

            //recompensa de plot (spell)
            if (rand.nextDouble() < 0.2) {
                String spell = GameData.getRandomSpell();
                hero.learnSpell(spell);
                log("Learned Spell: " + spell);
            }

            hero.setCurrentQuest(null);
            currentAction = "Returning to Town...";
        }
    }

    private Item generateItem(){
        Item.Slot[] slots = Item.Slot.values();
        Item.Slot slot = slots[rand.nextInt(slots.length)];
        String name = RandomNameGenerator.randomItemName();
        int bonus = 1 + (hero.getLevel() / 2) + rand.nextInt(3);
        return new Item(name, slot, bonus, "STR");
    }

    private void log(String msg){
        if (onLogUpdate != null) onLogUpdate.accept(msg);
    }

    public int getActionProgress(){return actionProgress;}
    public int getActionMax(){return actionMax;}
    public String getCurrentAction(){return currentAction;}
}