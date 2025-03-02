package com.example.orproject;

import java.util.Random;

public class Deck extends Shape{


    private static final Card[] arr = new Card[36];

    public Deck()
    {
        Card[]arr = new Card[36];
    }
    public void AddForDeck(Card[]arr) {


        arr[0] = new Card("Animals", "Lion", 1);
        arr[1] = new Card("Animals", "Giraffe", 2);
        arr[2] = new Card("Animals", "Elephant", 3);
        arr[3] = new Card("Animals", "Rabbit", 4);
        arr[4] = new Card("Cities", "Milano", 5);
        arr[5] = new Card("Cities", "Tel Aviv", 6);
        arr[6] = new Card("Cities", "Hong Kong", 7);
        arr[7] = new Card("Cities", "New York", 8);
        arr[8] = new Card("Countries", "Israel", 9);
        arr[9] = new Card("Countries", "Mexico", 10);
        arr[10] = new Card("Countries", "Italy", 11);
        arr[11] = new Card("Countries", "France", 12);
        arr[12] = new Card("Food", "Pasta", 13);
        arr[13] = new Card("Food", "Hamburger", 14);
        arr[14] = new Card("Food", "Pizza", 15);
        arr[15] = new Card("Food", "Rozelach", 16);
        arr[16] = new Card("Colors", "Black", 17);
        arr[17] = new Card("Colors", "White", 18);
        arr[18] = new Card("Colors", "Red", 19);
        arr[19] = new Card("Colors", "Blue", 20);
        arr[20] = new Card("Transportations", "Boat", 21);
        arr[21] = new Card("Transportations", "Bicycle", 22);
        arr[22] = new Card("Transportations", "Car", 23);
        arr[23] = new Card("Transportations", "Airplane", 24);
        arr[24] = new Card("Numbers", "Ten", 25);
        arr[25] = new Card("Numbers", "Five", 26);
        arr[26] = new Card("Numbers", "Two", 27);
        arr[27] = new Card("Numbers", "One", 28);
        arr[28] = new Card("Clothing", "T-shirt", 29);
        arr[29] = new Card("Clothing", "Pants", 30);
        arr[30] = new Card("Clothing", "Shoes", 31);
        arr[31] = new Card("Clothing", "Hat", 32);
        arr[32] = new Card("Electronic Devices", "Phone", 33);
        arr[33] = new Card("Electronic Devices", "Computer", 34);
        arr[34] = new Card("Electronic Devices", "Television", 35);
        arr[35] = new Card("Electronic Devices", "Tablet", 36);


    }

    public void Shuffle(Card[]arr){
            Random random = new Random();

            for (int i = arr.length - 1; i > 0; i--) {
                // Generate a random index between 0 and i
                int randomIndex = random.nextInt(i + 1);

                // Swap elements
                Card temp = arr[i];
                arr[i] = arr[randomIndex];
                arr[randomIndex] = temp;

    }





}}
