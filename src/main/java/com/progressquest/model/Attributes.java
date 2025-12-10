package com.progressquest.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Attributes {
    private int strength;
    private int dexterity;
    private int intelligence;
    private int stamina;
    private int charisma;

    private final Map<String, Integer> stats;
    private final Random rand = new Random();

    public Attributes() {
        stats = new LinkedHashMap<>();
        stats.put("STR", 0);
        stats.put("CON", 0);
        stats.put("DEX", 0);
        stats.put("INT", 0);
        stats.put("WIS", 0);
        stats.put("CHA", 0);
    }

    public void roll(){
        for (String key : stats.keySet()) {
            stats.put(key, 3 + rand.nextInt(16));
        }
    }

    public void increaseRandom() {
        String[] keys = stats.keySet().toArray(new String[0]);
        String key = keys[rand.nextInt(keys.length)];
        stats.put(key, stats.get(key) + 1);
    }

    public int get(String key) {return stats.getOrDefault(key, 0);}
    public Map<String, Integer> getAll(){return stats;}

    public int getTotal(){
        return stats.values().stream().mapToInt(Integer::intValue).sum();
    }
}