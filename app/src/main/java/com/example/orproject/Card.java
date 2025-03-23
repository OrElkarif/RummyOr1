package com.example.orproject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Card extends Shape {
    private String Catagory;
    private String CardName;
    private int id;

    private static final String[][] CATEGORY_CARDS = {
            {"Animals", "Lion", "Giraffe", "Elephant", "Rabbit"},
            {"Cities", "Milano", "Tel Aviv", "Hong Kong", "New York"},
            {"Countries", "Israel", "Mexico", "Italy", "France"},
            {"Food", "Pasta", "Hamburger", "Pizza", "Rozelach"},
            {"Colors", "Black", "White", "Red", "Blue"},
            {"Transportations", "Boat", "Bicycle", "Car", "Airplane"},
            {"Numbers", "Ten", "Five", "Two", "One"},
            {"Clothing", "T-shirt", "Pants", "Shoes", "Hat"},
            {"Electronic Devices", "Phone", "Computer", "Television", "Tablet"}
    };



    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public Card(String catagory, String cardName, int id) {
        super();
        this.Catagory = catagory;
        this.CardName = cardName;
        this.id = id;
    }

    public String getCatagory() { return Catagory; }
    public String getCardName() { return CardName; }
    public int getId() { return id; }

    public boolean equals(Card othercard) {
        return (othercard.getCatagory().equals(this.getCatagory()) &&
                othercard.getId() == this.getId() &&
                othercard.getCardName().equals(this.getCardName()));
    }

    @Override
    public void draw(Canvas canvas) {
        Paint p = new Paint();
        int color = Color.RED;
        p.setColor(color);

        canvas.drawRect(x, y, x+350, y+550, p);

        p.setColor(Color.BLACK);
        p.setTextSize(55);

        canvas.drawText(Catagory, x+10, y+70, p);
        canvas.drawText(CardName, x+10, y+130, p);

        p.setTextSize(45);
        p.setColor(Color.DKGRAY);

        for (String[] categoryGroup : CATEGORY_CARDS) {
            if (categoryGroup[0].equals(Catagory)) {
                int verticalOffset = 190;
                canvas.drawText("Others:", x+10, verticalOffset + y, p);

                for (int i = 1; i < categoryGroup.length; i++) {
                    if (!categoryGroup[i].equals(CardName)) {
                        verticalOffset += 55;
                        canvas.drawText(categoryGroup[i], x+30, verticalOffset + y, p);
                    }
                }
                break;
            }
        }
    }

    public boolean isUserTouchMe(int xu, int yu) {
        return xu > x && xu < x + 260 && yu > y && yu < y + 400;
    }
}