package com.example.orproject;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Card extends Shape{
    private String Catagory;
    private String CardName;
    private int id;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    int x = 30;
    int y = 1300;

    private boolean isSelected = false;

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Card(String catagory, String cardName, int id) {
        super();
        this.Catagory = catagory;
        this.CardName = cardName;
        this.id = id;
        Paint p = new Paint();
        p.setColor(Color.RED);
    }

    public String getCatagory() {
        return Catagory;
    }

    public String getCardName() {
        return CardName;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Card othercard){
        return (othercard.getCatagory()==this.getCatagory()&&othercard.getId()==this.getId()&&othercard.getCardName()==this.getCardName());
    }

    public void draw(Canvas canvas){
        Paint p = new Paint();
        int color = Color.RED;
        p.setColor(color);
        canvas.drawRect(x,y,x+260,y+400,p);
        p.setColor(Color.BLACK);
        p.setTextSize(50);
        canvas.drawText(Catagory, x+5, y+50 ,p);
        canvas.drawText(CardName, x+5, y+ 90,p);
    }

    public boolean isUserTouchMe(int xu, int yu) {
        return xu > x && xu < x + 260 && yu > y && yu < y + 400;
    }
}