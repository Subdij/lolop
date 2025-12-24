package com.example.lolop.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChampionMapper {
    private static final Map<String, List<String>> manualRoles = new HashMap<>();

    static {
        // Top
        manualRoles.put("Top", Arrays.asList(
            "Aatrox", "Akali", "Aurora", "Camille", "Chogath", "Darius", "DrMundo", "Fiora", 
            "Gangplank", "Garen", "Gnar", "Gragas", "Gwen", "Heimerdinger", "Illaoi", "Irelia", 
            "Jax", "Jayce", "Ksante", "Karma", "Kayle", "Kennen", "Kled", "Malphite", 
            "Mordekaiser", "Nasus", "Olaf", "Ornn", "Pantheon", "Poppy", "Quinn", "Renekton", 
            "Riven", "Rumble", "Shen", "Singed", "Sion", "Teemo", "Tryndamere", "Urgot", 
            "Vayne", "Yorick", "MonkeyKing"
        ));
        
        // Jungle
        manualRoles.put("Jungle", Arrays.asList(
            "Amumu", "Belveth", "Brand", "Briar", "Diana", "Ekko", "Elise", "Evelynn", 
            "Fiddlesticks", "Gragas", "Graves", "Hecarim", "Ivern", "JarvanIV", "Jax", 
            "Karthus", "Kayn", "Khazix", "Kindred", "LeeSin", "Lillia", "Maokai", 
            "MasterYi", "Nidalee", "Nocturne", "Nunu", "Pantheon", "Poppy", "Rammus", 
            "RekSai", "Rengar", "Sejuani", "Shaco", "Shyvana", "Skarner", "Taliyah", 
            "Talon", "Teemo", "Trundle", "Udyr", "Vi", "Viego", "Volibear", "Warwick", 
            "MonkeyKing", "XinZhao", "Zac", "Zed"
        ));
        
        // Mid
        manualRoles.put("Mid", Arrays.asList(
            "Ahri", "Akali", "Akshan", "Anivia", "Annie", "AurelionSol", "Aurora", "Azir", "Brand", "Cassiopeia", 
            "Corki", "Diana", "Ekko", "Fizz", "Galio", "Gragas", "Heimerdinger", "Hwei", "Irelia", "Jayce", 
            "Karma", "Kassadin", "Katarina", "Leblanc", "Lissandra", "Lux", "Malphite", "Malzahar", "Naafiri", 
            "Neeko", "Orianna", "Pantheon", "Qiyana", "Rumble", "Ryze", "Smolder", "Swain", "Sylas", "Syndra", 
            "Taliyah", "Talon", "Taric", "Tristana", "TwistedFate", "Veigar", "Velkoz", "Vex", "Viktor", 
            "Vladimir", "Xerath", "Yasuo", "Yone", "Zed", "Ziggs", "Zoe"
        ));
        
        // ADC (Bot)
        manualRoles.put("Bot", Arrays.asList(
            "Aphelios", "Ashe", "Caitlyn", "Corki", "Draven", "Ezreal", "Graves", "Jhin", "Jinx", 
            "Kaisa", "Kalista", "Kindred", "KogMaw", "Lucian", "MissFortune", "Samira", "Senna", 
            "Quinn", "Sivir", "Tristana", "Twitch", "Varus", "Vayne", "Xayah", "Zeri"
        ));
        
        // Support
        manualRoles.put("Support", Arrays.asList(
            "Alistar", "Amumu", "Anivia", "Annie", "Ashe", "Bard", "Blitzcrank", "Brand", "Braum", "Camille", 
            "Fiddlesticks", "Galio", "Heimerdinger", "Hwei", "Ivern", "Janna", "Karma", "Leona", "Lulu", "Lux", 
            "Malphite", "Maokai", "Milio", "Morgana", "Nami", "Nautilus", "Neeko", "Pantheon", "Pyke", "Rakan", 
            "Rell", "Renata", "Senna", "Seraphine", "Shaco", "Shen", "Sona", "Soraka", "Swain", "Syndra", 
            "TahmKench", "Taric", "Teemo", "Thresh", "Twitch", "Veigar", "Velkoz", "Xerath", "Yuumi", "Zac", 
            "Zilean", "Zoe"
        ));
    }

    public static boolean isChampionInRole(String championId, String role) {
        if (!manualRoles.containsKey(role)) return false;
        return manualRoles.get(role).contains(championId);
    }
}