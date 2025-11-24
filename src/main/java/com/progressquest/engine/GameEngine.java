package com.progressquest.engine;

import com.progressquest.model.Attributes;
import com.progressquest.model.Character;
import com.progressquest.model.Item;
import com.progressquest.model.Monster;
import com.progressquest.model.Quest;
import com.progressquest.util.RandomNameGenerator;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameEngine {
    private final Character hero;
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;
    private int turn = 0;

    public GameEngine() {
        Attributes attrs = new Attributes(5, 5, 5, 5, 5);
        this.hero = new Character("Herói", "Humano", "Aventureiro", attrs);
        Quest initial = new Quest("Primeira Missão", "Mate 3 monstros", 3, 150, null);
        hero.setCurrentQuest(initial);
    }

    //método para rodar no console (blocking)
    public void startConsole() {
        System.out.println("=== Progress Quest (Console) ===");
        System.out.println("Personagem: " + hero.getName());
        System.out.println("Iniciando simulação (aperte Ctrl+C para parar)\n");

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::gameTick, 0, 2, TimeUnit.SECONDS);

        //mantém a aplicação viva
        try {
            while (!scheduler.isShutdown()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) { }
    }

    //isso é para uso pela UI: start/stop expostos pela UI diretamente (UI usa os métodos tick etc)
    public void startHeadlessScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::gameTick, 0, 2, TimeUnit.SECONDS);
    }

    public void stopScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    public Character getHero() {
        return hero;
    }

    private void gameTick() {
        try {
            turn++;
            System.out.println("--- Turno " + turn + " ---");

            if (hero.getCurrentQuest() != null && !hero.getCurrentQuest().isCompleted()) {
                if (random.nextDouble() < 0.7) {
                    Monster m = spawnMonster();
                    System.out.println("Encontrou um monstro: " + m.getName() + " (nível " + m.getLevel() + ")");
                    long xp = m.getRewardXP();
                    hero.gainExperience(xp);
                    System.out.println("Derrotou o monstro! Ganhou " + xp + " XP.");
                    hero.getCurrentQuest().registerKill();
                    if (hero.getCurrentQuest().isCompleted()) {
                        System.out.println("Missão completada: " + hero.getCurrentQuest().getTitle());
                        hero.gainExperience(hero.getCurrentQuest().getRewardXP());
                        if (hero.getCurrentQuest().getRewardItem() != null) {
                            hero.addItem(hero.getCurrentQuest().getRewardItem());
                            System.out.println("Recebeu item: " + hero.getCurrentQuest().getRewardItem().getName());
                        }
                        hero.setCurrentQuest(new Quest("Missão aleatória", "Mate 5 monstros", 5, 300, null));
                    }
                } else {
                    Item it = generateRandomItem();
                    hero.addItem(it);
                    System.out.println("Encontrou item: " + it.getName());
                }
            } else {
                if (random.nextDouble() < 0.5) {
                    hero.setCurrentQuest(new Quest("Missão gerada", "Mate 4 monstros", 4, 200, null));
                    System.out.println("Nova missão aceita: " + hero.getCurrentQuest().getTitle());
                }
            }

            System.out.println(hero.status());
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
            if (scheduler != null) scheduler.shutdownNow();
        }
    }

    private Monster spawnMonster() {
        String name = RandomNameGenerator.randomMonsterName();
        int level = Math.max(1, hero.getLevel() + (int) (random.nextGaussian() * 2));
        long rewardXP = 20 + level * 10;
        double dropChance = Math.min(0.8, 0.2 + (level * 0.01));
        return new Monster(name, Math.max(1, level), rewardXP, dropChance);
    }

    private Item generateRandomItem() {
        String name = RandomNameGenerator.randomItemName();
        Item.Type type = random.nextBoolean() ? Item.Type.WEAPON : Item.Type.ARMOR;
        Attributes bonus = new Attributes(random.nextInt(3), random.nextInt(3), random.nextInt(3), 0, 0);
        return new Item(name, type, bonus);
    }
}