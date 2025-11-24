package com.progressquest.engine;

import com.progressquest.model.*;
import com.progressquest.util.RandomNameGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameEngine {
    private final Character hero;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Random random = new Random();
    private int turn = 0;

    public GameEngine() {
        Attributes attrs = new Attributes(5,5,5,5,5);
        this.hero = new Character("Herói", "Meio-Elfo", "Guerreiro Bárbaro Voodoo", attrs);

        Quest initialQuest = new Quest("Primeiras Caçadas", "Mate 3 monstros aleatórios", 3, 100, null);
        hero.setCurrentQuest(initialQuest);
    }
    public void start() {
        System.out.println("=== Progress Quest (simulação) ===");
        System.out.println("Personagem: ", + hero.getName() + " _ " + hero.getRace() + " " + hero.getClazz());
        System.out.println("Iniciando simulação... (pressione Ctrl + C para sair)\n");
        scheduler.scheduleAtFixedRate(this::gameTik, 0, 2, TimeUNit.SECONDS);
    }
    private void gameTIck() {
        try {
            turn ++;
            System.out.println("--- Turno " + turn + " ---");

            //isso vai decidir uma ação (se vai encontrar monstro ou prosseguir missão)
            if (hero.getCurrentQuest() != null && !hero.getCurrentQuest().isCompleted()){
                //aqui vai ter 70% de enfrentar um monstro que conta para a missão
                if (random.nextDouble() < 0.7) {
                    Monster m = spawnMonster();
                    System.out.println("Encontrou um monstro: " + m.getName() + "nível " + m.getLevel() + ")");
                    long xp = m.getRewardXP();
                    hero.gainExperience(xp);
                    System.out.println("Derrotou o monstro! Ganhou " + xp + " XP.");
                    hero.getCurrentQuest().registerKill();
                    if (hero.getCurrentQuest().isCompleted()) {
                        System.out.println("Missão completada: " + hero.getCurrentQuest().getTitle());
                        hero.gainExperience (hero.getCurrentQuest().getRewardXP());
                        if (hero.getCurrentQuest().getRewardItem() != null) {
                            hero.addItem(hero.getCurrentQuest().getRewardItem());
                            System.out.println("Recebeu item de recompensa: " + hero.getCurrentQuest().getRewardItem().getName());
                        }
                        //vai criar uma nova missão simples
                        hero.setCurrentQuest(new Quest("Missão Aleatória", "Mate 5 monstros", 5, 300, null));
                    }
                } else {
                    //30% de chance de achar item
                    Item it = generateRandomItem();
                    hero.addItem(it);
                    System.out.println("Encontrou item: " + it.getName());
                }
            }
        }
    }
}