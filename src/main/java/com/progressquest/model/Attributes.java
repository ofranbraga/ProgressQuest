package com.progressquest.model;

public class Attributes{
    private int strength;
    private int dexterity;
    private int intelligence;
    private int stamina;
    private int charisma;

    public Attributes(int strength, int dexterity, int intelligence, int stamina, int charisma){
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.stamina = stamina;
        this.charisma = charisma;
    }

    public void addStrength(int v){ this.strength += v; }
    public void addDexterity(int v){ this.dexterity += v; }
    public void addIntelligence(int v){ this.intelligence += v; }
    public void addStamina(int v){ this.stamina += v; }
    public void addCharisma(int v){ this.charisma += v; }

    public int getStrength() { return strength; }
    public int getDexterity() { return dexterity; }
    public int getIntelligence() { return intelligence; }
    public int getStamina() { return stamina; }
    public int getCharisma() { return charisma; }
}