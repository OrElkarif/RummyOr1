package com.example.orproject;

import android.graphics.Canvas;

public class Shape {//בינתיים הפעולה כמעט ולא בשימוש, מעדיף לא לגעת בינתיים כדי לא להרוס בטעות את המשחק
    public Shape() {
        this.x = x;
        this.y = y;
    }

    protected int x;
    protected int y;

    public void draw(Canvas canvas){


    }
    public boolean isUserTouchMe(int xu, int yu) {
        return xu > x && xu < x + 260 && yu > y && yu < y + 400;
    }
}
