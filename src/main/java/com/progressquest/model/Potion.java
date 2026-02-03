package com.progressquest.model;

/**
 * Simple consumable potion.
 *
 * <p>Kept intentionally lightweight: the UI and engine treat it as an Item in the
 * inventory list, but it is not equippable and can be consumed during combat turns.</p>
 */
public class Potion extends Item {

    public enum Kind {
        HEALTH, MANA
    }

    private final Kind kind;
    private final double restorePercent; // 0.0 - 1.0
    private final long price;

    public Potion(String name, Kind kind, double restorePercent, long price) {
        // Slot.POTION is used only to identify consumables.
        super(name, Slot.POTION, 0, "");
        this.kind = kind;
        this.restorePercent = Math.max(0.0, Math.min(1.0, restorePercent));
        this.price = Math.max(0L, price);
    }

    public Kind getKind() { return kind; }
    public double getRestorePercent() { return restorePercent; }
    public long getPrice() { return price; }

    @Override
    public String toString() {
        int pct = (int)Math.round(restorePercent * 100.0);
        return getName() + " (" + kind + " +" + pct + "%)";
    }
}
