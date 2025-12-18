package com.progressquest.data;

import java.util.*;

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

    //SISTEMA DE MAPAS E MONSTROS POR REGIÃO
    public static final Map<String, List<String>> MAPS = new LinkedHashMap<>();
    static {
        MAPS.put("Green Fields", Arrays.asList("Giant Rat", "Snake", "Wild Wolf", "Kobold"));
        MAPS.put("Dark Forest", Arrays.asList("Goblin", "Spider", "Bandit", "Orc"));
        MAPS.put("Deep Cave", Arrays.asList("Skeleton", "Bat", "Troll", "Slime"));
        MAPS.put("Volcano", Arrays.asList("Fire Elemental", "Dragon Whelp", "Lava Golem", "Imp"));
    }

    public static final List<String> QUEST_VERBS = Arrays.asList("Exterminate", "Hunt", "Slay", "Destroy");

    private static final Random rand = new Random();

    public static String getRandomRace() { return RACES.get(rand.nextInt(RACES.size())); }
    public static String getRandomClass() { return CLASSES.get(rand.nextInt(CLASSES.size())); }
    public static String getRandomSpell() { return SPELLS.get(rand.nextInt(SPELLS.size())); }

    //pega um monstro aleatório do mapa onde o jogador está
    public static String getMonsterFromMap(String mapName) {
        List<String> mobs = MAPS.getOrDefault(mapName, MAPS.get("Green Fields"));
        return mobs.get(rand.nextInt(mobs.size()));
    }

    //gera um monstro alvo para a Quest (pode ser de qualquer mapa, forçando viagem)
    public static String generateQuestTarget() {
        List<String> allMaps = new ArrayList<>(MAPS.keySet());
        String randomMap = allMaps.get(rand.nextInt(allMaps.size()));
        List<String> mobs = MAPS.get(randomMap);
        return mobs.get(rand.nextInt(mobs.size()));
    }

    public static String generateQuestTitle(String target) {
        return QUEST_VERBS.get(rand.nextInt(QUEST_VERBS.size())) + " " + target;
    }
}