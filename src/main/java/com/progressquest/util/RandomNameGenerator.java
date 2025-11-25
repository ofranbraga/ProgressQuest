package com.progressquest.util;

import java.security.PublicKey;
import java.util.Random;

public class RandomNameGenerator {
    private static final String[] MONSTER_PREFIX = {"Venenoso", "Gigante", "Sombrio", "Feral", "Arcaico"};
    private static final String[] MONSTER_BASE = {"Gargula", "Goblin", "Arauto", "Warg", "Espectro"};
    private static final String[] ITEM_ADJ = {"Rugente", "Silencioso", "Arcano", "Enferrujado", "Mítico"};
    private static final String[] ITEM_BASE = {"Sabre", "Elmo", "Cota", "Grevas", "Amuleto"};
    private static final Random rnd = new Random();

    public static String randomMonsterName() {
        return MONSTER_PREFIX[rnd.nextInt(MONSTER_PREFIX.length)] + " " + MONSTER_BASE[rnd.nextInt(MONSTER_BASE.length)];
    }

    public static String randomItemName() {
        return ITEM_ADJ[rnd.nextInt(ITEM_ADJ.length)] +  " " + ITEM_BASE[rnd.nextInt(ITEM_BASE.length)];
    }
}