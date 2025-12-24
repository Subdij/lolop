package com.example.lolop.model;

import java.util.Map;

public class ItemResponse {
    private String version;
    private Map<String, Item> data;

    public String getVersion() { return version; }
    public Map<String, Item> getData() { return data; }
}