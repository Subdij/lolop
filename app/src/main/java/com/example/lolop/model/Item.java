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
    private List<String> into;

    private Map<String, Boolean> maps;
    private Boolean inStore;
    private String requiredChampion;

    /** Définit l'identifiant de l'objet. */
    public void setId(String id) {
        this.id = id;
    }

    /** Retourne l'identifiant de l'objet. */
    public String getId() {
        return id;
    }

    /** Retourne le nom de l'objet. */
    public String getName() {
        return name;
    }

    /** Retourne la description HTML de l'objet. */
    public String getDescription() {
        return description;
    }

    /** Retourne la description texte brut de l'objet. */
    public String getPlaintext() {
        return plaintext;
    }

    /** Retourne les informations sur l'image de l'objet. */
    public Image getImage() {
        return image;
    }

    /** Retourne la liste des tags de l'objet. */
    public List<String> getTags() {
        return tags;
    }

    /** Retourne les informations sur le prix de l'objet. */
    public Gold getGold() {
        return gold;
    }

    /**
     * Retourne la liste des IDs des objets en lesquels cet objet peut être
     * transformé.
     */
    public List<String> getInto() {
        return into;
    }

    /** Retourne la liste des cartes où l'objet est disponible. */
    public Map<String, Boolean> getMaps() {
        return maps;
    }

    /** Indique si l'objet est disponible dans la boutique. */
    public boolean isInStore() {
        return inStore == null || inStore;
    } // Default to true

    /**
     * Retourne le nom du champion requis pour acheter cet objet (si applicable).
     */
    public String getRequiredChampion() {
        return requiredChampion;
    }

    public static class Image implements Serializable {
        private String full;

        public String getFull() {
            return full;
        }
    }

    public static class Gold implements Serializable {
        private int total;
        private int base;
        private int sell;
        private boolean purchasable;

        public int getTotal() {
            return total;
        }

        public int getBase() {
            return base;
        }

        public int getSell() {
            return sell;
        }

        public boolean isPurchasable() {
            return purchasable;
        }
    }
}