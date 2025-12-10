package com.progressquest.data;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameData {
    public static final List<String> RACES = Arrays.asList(
            "Half Orc", "Half Man", "Half Halfling", "Double Hobbit", "Hob-Hobbit",
            "Low Elf", "Dung Elf", "Talking Pony", "Gyrognome", "Lesser Dwarf",
            "Crested Dwarf", "Eel Man", "Panda Man", "Trans-Kobold", "Enchanted Motorcycle",
            "Will o' the Wisp", "Battle-Finch", "Double Wookiee", "Skraeling", "Demicanadian", "Land Squid"
    );

    public static final List<String> CLASSES = Arrays.asList(
            "Ur-Paladin", "Voodoo Princess", "Robot Monk", "Mu-Fu Monk",
            "Mage Illusioner", "Shiv-Knight", "Inner Mason", "Fighter/Organist",
            "Puma Burglar", "Runeloremaster", "Hunter Strangler", "Battle-Felon",
            "Tickle-Mimic", "Slow Poisoner", "Bastard Lunatic", "Lowling", "Birdrider", "Vermineer"
    );

    public static final List<String> SPELLS = Arrays.asList(
            "Slime Finger", "Rabbit Punch", "Good Move", "Sadness",
            "Seasick", "Gyp", "Shoelaces", "Innoculate", "Cone of Paste",
            "Spectral Miasma", "Clever Dream", "Haste", "Slow", "Polymorph Other"
    );

    public static final List<String> MONSTERS = Arrays.asList(
            "Rato Gigante", "Kobold", "Goblin", "Orc", "Banshee", "Dragão de Papel",
            "Basilisco Vesgo", "Beholder Cego", "Esqueleto de Gelatina", "Homem-Peixe",
            "Múmia de Papel Higiênico", "Vampiro Vegano", "Succubus Entediada"
    );

    public static final List<String> TITULOS_QUEST = Arrays.asList("Fetch me", "Deliver", "Seek", "Placate", "Exterminate");
    public static final List<String> ALVOS_QUEST = Arrays.asList("a newspaper", "this bandage", "the coin", "the Nymphs", "the Rats");

    private static final Random rand = new Random();

    public static String getRandomRace(){return RACES.get(rand.nextInt(RACES.size()));}
    public static String getRandomClass(){return CLASSES.get(rand.nextInt(CLASSES.size()));}
    public static String getRandomMonster() { return MONSTERS.get(rand.nextInt(MONSTERS.size())); }
    public static String getRandomSpell() { return SPELLS.get(rand.nextInt(SPELLS.size())); }

    public static String generateQuestName(){
        return TITULOS_QUEST.get(rand.nextInt(TITULOS_QUEST.size())) + " " +
                ALVOS_QUEST.get(rand.nextInt(ALVOS_QUEST.size()));
    }
}
