package com.example.lolop.model;

import java.io.Serializable;

public class Item implements Serializable {
    private String name;
    private String description;
    private String plaintext;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPlaintext() { return plaintext; }
}