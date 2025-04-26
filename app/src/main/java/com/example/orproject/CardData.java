package com.example.orproject;

public class CardData {
    public String category;
    public String name;
    public int id;

    public CardData() {
        // Empty constructor required for Firebase
    }

    public CardData(String category, String name, int id) {
        this.category = category;
        this.name = name;
        this.id = id;
    }
}
