package com.progressquest.model;

public class Item {
    public enum Type { WEAPON, ARMOR, CONSUMABLE }
    public enum Slot { MAIN_HAND, OFF_HAND, HEAD, CHEST, LEGS }

    private final String name;
    private final Type type;
    private final Attributes bonus;

    public Item(String name, Type type, Attributes bonus) {
        this.name = name;
        this.type = type;
        this.bonus = bonus;
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public Attributes getBonus() { return bonus; }

    public Slot getSlot() {
        if (type == Type.WEAPON) return Slot.MAIN_HAND;
        if (type == Type.ARMOR) return Slot.CHEST;
        return Slot.OFF_HAND;
    }
}