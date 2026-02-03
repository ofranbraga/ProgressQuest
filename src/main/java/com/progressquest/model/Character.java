package com.progressquest.model;

import com.progressquest.model.damage.DamageStrategy;
import com.progressquest.model.damage.MagicDamage;
import com.progressquest.model.damage.PhysicalDamage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The player character for Idle mode.
 *
 * <p>All 2D movement/physics fields and methods were removed.</p>
 */
public class Character extends Entity {
    private String race;
    private String clazz;
    private long experience;
    private int attributePoints;

    // combat resources
    private int mpMax;
    private int mpCurrent;
    private String currentMap;

    // Strategy: damage calculation can vary by class
    private DamageStrategy damageStrategy;

    private final Attributes attributes;
    private final List<Item> inventory;
    private final Map<Item.Slot, Item> equipment;
    private final List<String> spellBook;

    private Quest currentQuest;

    // gold
    private long gold;

    public Character(Attributes attrs) {
        this.attributes = attrs;
        this.inventory = new ArrayList<>();
        this.equipment = new HashMap<>();
        this.spellBook = new ArrayList<>();

        this.level = 1;
        this.experience = 0;
        this.attributePoints = 0;
        this.currentMap = "Green Fields";
        this.gold = 0L;

        // default strategy (may be overridden in init)
        this.damageStrategy = new PhysicalDamage();

        recalcStats();
        this.hpCurrent = this.hpMax;
        this.mpCurrent = this.mpMax;
    }

    /**
     * Called by UI after creation.
     *
     * <p>Signature preserved.</p>
     */
    public void init(String name, String race, String clazz) {
        this.name = name;
        this.race = race;
        this.clazz = clazz;

        // Strategy selection based on class "flavor"
        if (clazz != null) {
            String c = clazz.toLowerCase();
            if (c.contains("mage") || c.contains("wizard") || c.contains("sor")
                    || c.contains("cleric") || c.contains("warlock")) {
                this.damageStrategy = new MagicDamage();
            } else {
                this.damageStrategy = new PhysicalDamage();
            }
        }

        recalcStats();
        if (hpCurrent > hpMax) hpCurrent = hpMax;
        if (mpCurrent > mpMax) mpCurrent = mpMax;
    }

    public void recalcStats() {
        this.hpMax = 20 + (attributes.get("CON") * 3) + (level * 10);
        this.mpMax = 10 + (attributes.get("INT") * 2) + (attributes.get("WIS") * 2) + (level * 5);

        if (hpCurrent > hpMax) hpCurrent = hpMax;
        if (mpCurrent > mpMax) mpCurrent = mpMax;
    }

    public void heal(int amount) {
        this.hpCurrent += Math.max(0, amount);
        if (this.hpCurrent > hpMax) this.hpCurrent = hpMax;
    }

    /** Heal a percentage of max HP (e.g., 0.20 = 20% of max). */
    public void healPercent(double pct) {
        int amount = (int)Math.ceil(this.hpMax * Math.max(0.0, pct));
        heal(amount);
    }

    public void regenMana(int amount) {
        this.mpCurrent += Math.max(0, amount);
        if (this.mpCurrent > mpMax) this.mpCurrent = mpMax;
    }

    /** Regenerate a percentage of max MP (e.g., 0.20 = 20% of max). */
    public void regenManaPercent(double pct) {
        int amount = (int)Math.ceil(this.mpMax * Math.max(0.0, pct));
        regenMana(amount);
    }

    public boolean hasMana(int amount) {
        return mpCurrent >= Math.max(0, amount);
    }

    /** Spend mana if available; returns true if the cost was paid. */
    public boolean spendMana(int amount) {
        int cost = Math.max(0, amount);
        if (mpCurrent >= cost) {
            mpCurrent -= cost;
            return true;
        }
        return false;
    }

    /**
     * Computes outgoing damage.
     *
     * <p>Signature preserved for UI/engine compatibility.</p>
     */
    public int calculateDamage() {
        if (damageStrategy == null) damageStrategy = new PhysicalDamage();
        return damageStrategy.calculate(this);
    }

    /** Allows swapping damage calculation at runtime. */
    public void setDamageStrategy(DamageStrategy strategy) {
        if (strategy != null) this.damageStrategy = strategy;
    }

    public DamageStrategy getDamageStrategy() {
        return damageStrategy;
    }

    public void gainExperience(long xp) {
        this.experience += xp;
        while (this.experience >= xpToNextLevel()) {
            levelUp();
        }
    }

    public long xpToNextLevel() {
        return 1000L * level;
    }

    private void levelUp() {
        this.experience -= xpToNextLevel();
        this.level++;
        this.attributePoints += 3;
        recalcStats();
        this.hpCurrent = this.hpMax;
        this.mpCurrent = this.mpMax;
    }

    public void lootItem(Item newItem) {
        // Consumables are never auto-equipped.
        if (newItem.getSlot() == Item.Slot.POTION) {
            inventory.add(newItem);
            return;
        }
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
}
