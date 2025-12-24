package com.example.lolop.model;

import java.io.Serializable;
import java.util.List;

public class Champion implements Serializable {
    private String id;
    private String key;
    private String name;
    private String title;
    private String blurb;
    private String lore;
    private Info info;
    private Image image;
    private List<String> tags;
    private List<Spell> spells;
    private Passive passive;
    private List<Recommended> recommended;
    private List<String> allytips;
    private List<String> enemytips;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getBlurb() { return blurb; }
    public String getLore() { return lore; }
    public Info getInfo() { return info; }
    public Image getImage() { return image; }
    public List<String> getTags() { return tags; }
    public List<Spell> getSpells() { return spells; }
    public Passive getPassive() { return passive; }
    public List<Recommended> getRecommended() { return recommended; }
    public List<String> getAllytips() { return allytips; }
    public List<String> getEnemytips() { return enemytips; }

    public static class Info implements Serializable {
        private int attack;
        private int defense;
        private int magic;
        private int difficulty;
        public int getDifficulty() { return difficulty; }
    }

    public static class Image implements Serializable {
        private String full;
        public String getFull() { return full; }
    }

    public static class Spell implements Serializable {
        private String id;
        private String name;
        private String description;
        private Image image;
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Image getImage() { return image; }
    }

    public static class Passive implements Serializable {
        private String name;
        private String description;
        private Image image;
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Image getImage() { return image; }
    }

    public static class Recommended implements Serializable {
        private String map;
        private List<Block> blocks;
        public String getMap() { return map; }
        public List<Block> getBlocks() { return blocks; }

        public static class Block implements Serializable {
            private String type;
            private List<Item> items;
            public String getType() { return type; }
            public List<Item> getItems() { return items; }

            public static class Item implements Serializable {
                private String id;
                private int count;
                public String getId() { return id; }
            }
        }
    }
}