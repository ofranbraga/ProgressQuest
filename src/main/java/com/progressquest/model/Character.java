package com.progressquest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Character {
    private String name;
    private String race;
    private String clazz;
    private int level;
    private long experience;

    //status derivados
    private int hpMax;
    private int mpMax;

    private final Attributes attributes;
    private final List<Item> inventory;
    private final Map<Item.Slot, Item> equipment;
    private final List<String> spellBook;

    private Quest currentQuest;
    private String currentPlot; //ex: "Prologue", "Act I"

    public Character(Attributes attrs) {
        this.attributes = attrs;
        this.inventory = new ArrayList<>();
        this.equipment = new HashMap<>();
        this.spellBook = new ArrayList<>();
        this.level = 1;
        this.experience = 0;
        this.currentPlot = "Prologue";
        recalcStats();
    }

    public void init(String name, String race, String clazz) {
        this.name = name;
        this.race = race;
        this.clazz = clazz;
    }

    public void recalcStats() {
        //HP baseado em CON, MP baseado em INT/WIS
        this.hpMax = 10 + (attributes.get("CON") * 2) + (level * 5);
        this.mpMax = 5 + (attributes.get("INT") + attributes.get("WIS")) + (level * 2);
    }

    public void gainExperience(long xp) {
        this.experience += xp;
        if (this.experience >= xpToNextLevel()) {
            levelUp();
        }
    }

    public long xpToNextLevel() {
        return 1000L * level;
    }

    private void levelUp() {
        this.experience -= xpToNextLevel();
        this.level++;
        this.attributes.increaseRandom(); //aumenta um atributo
        recalcStats();
    }

    //lógica core do PQ: Auto-equipar se for melhor
    public void lootItem(Item newItem) {
        Item current = equipment.get(newItem.getSlot());

        if (current == null || newItem.getBonus() > current.getBonus()) {
            //equipa o novo
            equipment.put(newItem.getSlot(), newItem);
            if (current != null) {
                inventory.add(current); // Guarda o velho
            }
        } else {
            //guarda o novo no inventário
            inventory.add(newItem);
        }
    }

    public void learnSpell(String spell) {
        if (!spellBook.contains(spell)) {
            spellBook.add(spell);
        }
    }

    //getters
    public String getName() { return name; }
    public String getRace() { return race; }
    public String getClazz() { return clazz; }
    public int getLevel() { return level; }
    public long getExperience() { return experience; }
    public Attributes getAttributes() { return attributes; }
    public int getHpMax() { return hpMax; }
    public int getMpMax() { return mpMax; }
    public List<Item> getInventory() { return inventory; }
    public Map<Item.Slot, Item> getEquipment() { return equipment; }
    public List<String> getSpellBook() { return spellBook; }
    public Quest getCurrentQuest() { return currentQuest; }
    public void setCurrentQuest(Quest q) { this.currentQuest = q; }
    public String getCurrentPlot() { return currentPlot; }
    public void setCurrentPlot(String p) { this.currentPlot = p; }
}