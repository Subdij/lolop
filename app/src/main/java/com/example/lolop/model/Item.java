package com.example.lolop.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Item implements Serializable {
    private String id;
    private String name;
    private String description;
    private String plaintext;


    private Image image;
    private List<String> tags;
    private Gold gold;
    private Map<String, Boolean> maps;
    private Boolean inStore;
    private String requiredChampion;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPlaintext() { return plaintext; }
    public Image getImage() { return image; }
    public List<String> getTags() { return tags; }
    public Gold getGold() { return gold; }
    public Map<String, Boolean> getMaps() { return maps; }
    public boolean isInStore() { return inStore == null || inStore; } // Default to true
    public String getRequiredChampion() { return requiredChampion; }

    public static class Image implements Serializable {
        private String full;
        public String getFull() { return full; }
    }

    public static class Gold implements Serializable {
        private int total;
        private int base;
        private int sell;
        private boolean purchasable;
        
        public int getTotal() { return total; }
        public int getBase() { return base; }
        public int getSell() { return sell; }
        public boolean isPurchasable() { return purchasable; }
    }
}