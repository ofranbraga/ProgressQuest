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

    //PONTOS DE ATRIBUTO
    private int attributePoints; // Pontos livres para gastar

    //status de Combate
    private int hpMax;
    private int hpCurrent;
    private int mpMax;
    private int mpCurrent;
    private String currentMap;

    private final Attributes attributes;
    private final List<Item> inventory;
    private final Map<Item.Slot, Item> equipment;
    private final List<String> spellBook;

    private Quest currentQuest;

    public Character(Attributes attrs) {
        this.attributes = attrs;
        this.inventory = new ArrayList<>();
        this.equipment = new HashMap<>();
        this.spellBook = new ArrayList<>();
        this.level = 1;
        this.experience = 0;
        this.attributePoints = 0; //começa zerado (distribuído na criação)
        this.currentMap = "Green Fields";
        recalcStats();

        this.hpCurrent = this.hpMax;
        this.mpCurrent = this.mpMax;
    }

    public void init(String name, String race, String clazz) {
        this.name = name;
        this.race = race;
        this.clazz = clazz;
    }

    public void recalcStats() {
        this.hpMax = 20 + (attributes.get("CON") * 3) + (level * 10);
        this.mpMax = 10 + (attributes.get("INT") * 2) + (attributes.get("WIS") * 2) + (level * 5);

        if (hpCurrent > hpMax) hpCurrent = hpMax;
        if (mpCurrent > mpMax) mpCurrent = mpMax;
    }

    //MÉTODOS PARA GASTAR PONTOS
    public boolean spendAttributePoint(String attrName) {
        if (attributePoints > 0) {
            attributes.increment(attrName);
            attributePoints--;
            recalcStats(); //recalcula HP/MP pois CON/INT podem ter mudado
            return true;
        }
        return false;
    }

    //COMBATE
    public void takeDamage(int dmg) {
        this.hpCurrent -= dmg;
        if (this.hpCurrent < 0) this.hpCurrent = 0;
    }

    public void heal(int amount) {
        this.hpCurrent += amount;
        if (this.hpCurrent > hpMax) this.hpCurrent = hpMax;
    }

    public void regenMana(int amount) {
        this.mpCurrent += amount;
        if (this.mpCurrent > mpMax) this.mpCurrent = mpMax;
    }

    public boolean isDead() { return hpCurrent <= 0; }

    public int calculateDamage() {
        int dmg = 2 + (attributes.get("STR") / 2);
        Item weapon = equipment.get(Item.Slot.WEAPON);
        if (weapon != null) dmg += weapon.getBonus();
        return dmg;
    }

    public void gainExperience(long xp) {
        this.experience += xp;
        while (this.experience >= xpToNextLevel()) {
            levelUp();
        }
    }

    public long xpToNextLevel() { return 1000L * level; }

    private void levelUp() {
        this.experience -= xpToNextLevel();
        this.level++;

        this.attributePoints += 3;

        recalcStats();
        //cura total no level up
        this.hpCurrent = this.hpMax;
        this.mpCurrent = this.mpMax;
    }

    public void lootItem(Item newItem) {
        Item current = equipment.get(newItem.getSlot());
        if (current == null || newItem.getBonus() > current.getBonus()) {
            equipment.put(newItem.getSlot(), newItem);
            if (current != null) inventory.add(current);
        } else {
            inventory.add(newItem);
        }
    }

    public void learnSpell(String spell) {
        if (!spellBook.contains(spell)) spellBook.add(spell);
    }

    public String getName() { return name; }
    public String getRace() { return race; }
    public String getClazz() { return clazz; }
    public int getLevel() { return level; }
    public long getExperience() { return experience; }
    public Attributes getAttributes() { return attributes; }
    public int getAttributePoints() { return attributePoints; } // Novo Getter

    public int getHpMax() { return hpMax; }
    public int getHpCurrent() { return hpCurrent; }
    public int getMpMax() { return mpMax; }
    public int getMpCurrent() { return mpCurrent; }

    public String getCurrentMap() { return currentMap; }
    public void setCurrentMap(String map) { this.currentMap = map; }

    public List<Item> getInventory() { return inventory; }
    public Map<Item.Slot, Item> getEquipment() { return equipment; }
    public List<String> getSpellBook() { return spellBook; }

    public Quest getCurrentQuest() { return currentQuest; }
    public void setCurrentQuest(Quest q) { this.currentQuest = q; }
}