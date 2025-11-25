package com.progressquest.model;

import java.lang.classfile.Attributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Character {
    private final String name;
    private final String race;
    private final String clazz;
    private int level;
    private int experience;
    private final Attributes attributes;
    private final List<Item> inventory = new ArrayList<>();
    private final Map<Item.Slot, Item> equipment = new HashMap<>();
    private Quest currentQuest;

    public Character(String name, String race, String clazz, Attributes attributes) {
        this.name = name;
        this.race = race;
        this.clazz = clazz;
        this.level = 1;
        this.experience = 0;
        this.attributes = attributes;
    }

    public void gainExperience(long xp) {
        if (xp <= 0) return;
        this.experience += xp;
        while (this.experience >= xpToNextLevel()) {
            this.experience -= xpToNextLevel;
            this.levelUp();
        }
    }

    private long xpToNextLevel() {
        return 100L *  this.level;
    }
    private void levelUp() {
        this.level++;
        this.attributes.addStrength(1);
        this.attributes.addStamina(1);
        System.out.println("*** " + name + " subiu para nível " + level + "! ***");
    }

    public void equipItem(Item item) {
        if (Item == null) return;
        this.Inventory.add(item);
    }

    public String status() {
        return String.format("%s - Nível %d | XP atual: %d/%d | Inventário: %d itens | Missão: %s", name, level, experience, xpToNextLevel(), inventoru.size(), currentQuest == null ? "Nenhuma" : currentQuest.getTitle());
    }

    public String getName() { return name; }
    public String getRace() { return race; }
    public String getClazz() { return clazz; }
    public int getLevel() { return level; }
    public long getExperience() { return experience; }
    public Attributes getAttributes() { return attributes; }
    public List<Item> getInventory() { return inventory; }
    public Map<Item.Slot, Item> getEquipment() { return equipment; }
    public Quest getCurrentQuest() { return currentQuest; }
    public void setCurrentQuest(Quest currentQuest) { this.currentQuest = currentQuest; }
}