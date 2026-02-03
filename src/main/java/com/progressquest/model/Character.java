package com.progressquest.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Character extends Entity {
    private String race;
    private String clazz;
    private long experience;
    private int attributePoints;

    //status de combate
    private int mpMax;
    private int mpCurrent;
    private String currentMap;

    //fisica 2D
    private double speed; //velocidade de movimento

    private final Attributes attributes;
    private final List<Item> inventory;
    private final Map<Item.Slot, Item> equipment;
    private final List<String> spellBook;

    private Quest currentQuest;

    // ouro do jogador
    private long gold;
    private DamageStrategy damageStrategy;

    public Character(Attributes attrs) {
        this.attributes = attrs;
        this.inventory = new ArrayList<>();
        this.equipment = new HashMap<>();
        this.spellBook = new ArrayList<>();
        this.level = 1;
        this.experience = 0;
        this.attributePoints = 0;
        this.currentMap = "Green Fields";
        this.damageStrategy = new PhysicalDamage();

        //posição inicial no mundo 2D
        this.x = 400;
        this.y = 300;
        this.speed = 3.0;

        // ouro inicial
        this.gold = 0L;

        recalcStats();
        this.hpCurrent = this.hpMax;
        this.mpCurrent = this.mpMax;
    }

    public void init(String name, String race, String clazz) {
        this.name = name;
        this.race = race;
        this.clazz = clazz;
        selectDamageStrategyForClass();
    }

    public void recalcStats() {
        this.hpMax = 20 + (attributes.get("CON") * 3) + (level * 10);
        this.mpMax = 10 + (attributes.get("INT") * 2) + (attributes.get("WIS") * 2) + (level * 5);
        //velocidade baseada em Destreza
        this.speed = 2.0 + (attributes.get("DEX") * 0.1);

        if (hpCurrent > hpMax) hpCurrent = hpMax;
        if (mpCurrent > mpMax) mpCurrent = mpMax;
    }

    //movimento
    public void move(double dx, double dy) {
        // normaliza o vetor para que movimento diagonal não seja mais rápido
        if (dx == 0 && dy == 0) return;
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return;
        double ndx = dx / len;
        double ndy = dy / len;
        double adjust = 1.7; // ajuste solicitado para padronizar velocidade diagonal
        this.x += ndx * speed * adjust;
        this.y += ndy * speed * adjust;
    }

    public void heal(int amount) {
        this.hpCurrent += amount;
        if (this.hpCurrent > hpMax) this.hpCurrent = hpMax;
    }

    public void regenMana(int amount) {
        this.mpCurrent += amount;
        if (this.mpCurrent > mpMax) this.mpCurrent = mpMax;
    }

    public int calculateDamage() {
        if (damageStrategy == null) {
            damageStrategy = new PhysicalDamage();
        }
        return damageStrategy.calculateDamage(this);
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

    public boolean spendAttributePoint(String attrName) {
        if (attributePoints > 0) {
            attributes.increment(attrName);
            attributePoints--;
            recalcStats();
            return true;
        }
        return false;
    }

    public void learnSpell(String spell) {
        if (!spellBook.contains(spell)) spellBook.add(spell);
    }

    public String getRace() { return race; }
    public String getClazz() { return clazz; }
    public long getExperience() { return experience; }
    public Attributes getAttributes() { return attributes; }
    public int getAttributePoints() { return attributePoints; }
    public int getMpMax() { return mpMax; }
    public int getMpCurrent() { return mpCurrent; }
    public String getCurrentMap() { return currentMap; }
    public void setCurrentMap(String map) { this.currentMap = map; }
    public List<Item> getInventory() { return inventory; }
    public Map<Item.Slot, Item> getEquipment() { return equipment; }
    public List<String> getSpellBook() { return spellBook; }
    public Quest getCurrentQuest() { return currentQuest; }
    public void setCurrentQuest(Quest q) { this.currentQuest = q; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }

    // gold management
    public long getGold() { return gold; }
    public void addGold(long amount) {
        if (amount <= 0) return;
        this.gold += amount;
    }
    public boolean spendGold(long amount) {
        if (amount <= 0) return true;
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    public void setDamageStrategy(DamageStrategy damageStrategy) {
        if (damageStrategy != null) {
            this.damageStrategy = damageStrategy;
        }
    }

    int getWeaponBonus() {
        Item weapon = equipment.get(Item.Slot.WEAPON);
        return weapon != null ? weapon.getBonus() : 0;
    }

    private void selectDamageStrategyForClass() {
        if (clazz == null) return;
        String normalized = clazz.toLowerCase();
        if (normalized.contains("mage")
                || normalized.contains("illusioner")
                || normalized.contains("voodoo")
                || normalized.contains("runelore")) {
            this.damageStrategy = new MagicDamage();
        } else {
            this.damageStrategy = new PhysicalDamage();
        }
    }
}
