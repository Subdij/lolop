package com.example.lolop.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Champion implements Parcelable {
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

    protected Champion(Parcel in) {
        id = in.readString();
        key = in.readString();
        name = in.readString();
        title = in.readString();
        blurb = in.readString();
        lore = in.readString();
        info = in.readParcelable(Info.class.getClassLoader());
        image = in.readParcelable(Image.class.getClassLoader());
        tags = in.createStringArrayList();
        spells = in.createTypedArrayList(Spell.CREATOR);
        passive = in.readParcelable(Passive.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(title);
        dest.writeString(blurb);
        dest.writeString(lore);
        dest.writeParcelable(info, flags);
        dest.writeParcelable(image, flags);
        dest.writeStringList(tags);
        dest.writeTypedList(spells);
        dest.writeParcelable(passive, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Champion> CREATOR = new Creator<Champion>() {
        @Override
        public Champion createFromParcel(Parcel in) {
            return new Champion(in);
        }

        @Override
        public Champion[] newArray(int size) {
            return new Champion[size];
        }
    };

    public static class Info implements Parcelable {
        private int attack;
        private int defense;
        private int magic;
        private int difficulty;
        public int getDifficulty() { return difficulty; }

        protected Info(Parcel in) {
            attack = in.readInt();
            defense = in.readInt();
            magic = in.readInt();
            difficulty = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(attack);
            dest.writeInt(defense);
            dest.writeInt(magic);
            dest.writeInt(difficulty);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Info> CREATOR = new Creator<Info>() {
            @Override
            public Info createFromParcel(Parcel in) {
                return new Info(in);
            }

            @Override
            public Info[] newArray(int size) {
                return new Info[size];
            }
        };
    }

    public static class Image implements Parcelable {
        private String full;
        public String getFull() { return full; }

        protected Image(Parcel in) {
            full = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(full);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Image> CREATOR = new Creator<Image>() {
            @Override
            public Image createFromParcel(Parcel in) {
                return new Image(in);
            }

            @Override
            public Image[] newArray(int size) {
                return new Image[size];
            }
        };
    }

    public static class Spell implements Parcelable {
        private String id;
        private String name;
        private String description;
        private Image image;
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Image getImage() { return image; }

        protected Spell(Parcel in) {
            id = in.readString();
            name = in.readString();
            description = in.readString();
            image = in.readParcelable(Image.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(description);
            dest.writeParcelable(image, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Spell> CREATOR = new Creator<Spell>() {
            @Override
            public Spell createFromParcel(Parcel in) {
                return new Spell(in);
            }

            @Override
            public Spell[] newArray(int size) {
                return new Spell[size];
            }
        };
    }

    public static class Passive implements Parcelable {
        private String name;
        private String description;
        private Image image;
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Image getImage() { return image; }

        protected Passive(Parcel in) {
            name = in.readString();
            description = in.readString();
            image = in.readParcelable(Image.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(description);
            dest.writeParcelable(image, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Passive> CREATOR = new Creator<Passive>() {
            @Override
            public Passive createFromParcel(Parcel in) {
                return new Passive(in);
            }

            @Override
            public Passive[] newArray(int size) {
                return new Passive[size];
            }
        };
    }
}
