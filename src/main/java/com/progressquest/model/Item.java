package com.progressquest.model;

public class Item {

    public enum Slot {
        WEAPON, SHIELD, HELM, HAUBERK, BRASSAIRTS, VAMBRACES,
        GAUNTLETS, GAMBESON, CUISSES, GREAVES, SOLLERETS
    }

    private final String name;
    private final Slot slot;
    private final int bonus; // Poder do item
    private final String statAffected; // Ex: STR

    public Item(String name, Slot slot, int bonus, String statAffected) {
        this.name = name;
        this.slot = slot;
        this.bonus = bonus;
        this.statAffected = statAffected;
    }

    public String getName() { return name; }
    public Slot getSlot() { return slot; }
    public int getBonus() { return bonus; }
    public String getStatAffected() { return statAffected; }

    @Override
    public String toString() {
        //formato para tooltip ou log: "Espada (+2 STR)"
        return String.format("%s %s %d", name, (bonus >= 0 ? "+" : ""), bonus);
    }

    //texto para aparecer na lista de equipamentos (ex:"Battle Axe")
    public String getDisplayString() {
        return name + " " + (bonus > 0 ? "+" + bonus : "");
    }
}
