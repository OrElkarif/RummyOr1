package com.example.orproject;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class FbModule {
    private FirebaseDatabase firebaseDatabase;
    private static DatabaseReference gameStateRef;
    private GameStateListener gameStateListener;

    public interface GameStateListener {
        void onPlayer1CardsChanged(ArrayList<Card> cards);
        void onPlayer2CardsChanged(ArrayList<Card> cards);
        void onPacketChanged(ArrayList<Card> cards);
        void onTurnChanged(String currentPlayer);
        void onPlayerScoreUpdated(String player, int score);
        void onBackgroundColorChanged(String color); // Add this line
    }

    public FbModule(GameStateListener listener) {
        this.gameStateListener = listener;
        firebaseDatabase = FirebaseDatabase.getInstance();
        gameStateRef = firebaseDatabase.getReference("gameState");
        setupListeners();
    }

    private void setupListeners() {
        // האזנה לקלפים של שחקן 1
        gameStateRef.child("backgroundColor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String color = snapshot.getValue(String.class);
                if (color != null && gameStateListener != null) {
                    gameStateListener.onBackgroundColorChanged(color);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // האזנה לקלפים של שחקן 2
        gameStateRef.child("player2Cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Card> cards = new ArrayList<>();
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    CardData cardData = cardSnapshot.getValue(CardData.class);
                    if (cardData != null) {
                        cards.add(new Card(cardData.category, cardData.name, cardData.id));
                    }
                }
                if (gameStateListener != null) {
                    gameStateListener.onPlayer2CardsChanged(cards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // האזנה לקופה
        gameStateRef.child("packet").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Card> cards = new ArrayList<>();
                for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                    CardData cardData = cardSnapshot.getValue(CardData.class);
                    if (cardData != null) {
                        cards.add(new Card(cardData.category, cardData.name, cardData.id));
                    }
                }
                if (gameStateListener != null) {
                    gameStateListener.onPacketChanged(cards);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // האזנה לתור
        gameStateRef.child("currentTurn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentPlayer = snapshot.getValue(String.class);
                if (gameStateListener != null && currentPlayer != null) {
                    gameStateListener.onTurnChanged(currentPlayer);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // האזנה לניקוד
        gameStateRef.child("playerScores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot scoreSnapshot : snapshot.getChildren()) {
                    String player = scoreSnapshot.getKey();
                    int score = scoreSnapshot.getValue(Integer.class);
                    if (gameStateListener != null) {
                        gameStateListener.onPlayerScoreUpdated(player, score);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // עדכון הקלפים של שחקן 1
    public void updatePlayer1Cards(ArrayList<Card> cards) {
        gameStateRef.child("player1Cards").setValue(serializeCards(cards));
    }

    // עדכון הקלפים של שחקן 2
    public void updatePlayer2Cards(ArrayList<Card> cards) {
        gameStateRef.child("player2Cards").setValue(serializeCards(cards));
    }

    // עדכון הקופה
    public void updatePacket(ArrayList<Card> cards) {
        gameStateRef.child("packet").setValue(serializeCards(cards));
    }

    // עדכון תור המשחק
    public static void updateTurn(String player) {
        gameStateRef.child("currentTurn").setValue(player);
    }

    // עדכון הניקוד של שחקן
    public void updatePlayerScore(String player, int score) {
        gameStateRef.child("playerScores").child(player).setValue(score);
    }

    // הוספת קלף לשחקן
    public void moveCardToPlayer(String player, Card card) {
        gameStateRef.child(player + "Cards").get().addOnSuccessListener(snapshot -> {
            ArrayList<Card> cards = new ArrayList<>();
            for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                CardData cardData = cardSnapshot.getValue(CardData.class);
                if (cardData != null) {
                    cards.add(new Card(cardData.category, cardData.name, cardData.id));
                }
            }
            cards.add(card); // מוסיפים את הקלף החדש
            gameStateRef.child(player + "Cards").setValue(serializeCards(cards)); // מעדכנים ב-Firebase
        });
    }


    // מחיקת כל הנתונים במשחק (למשחק חדש)
    public void resetGame() {
        gameStateRef.setValue(null);
    }

    // עוזר להמיר קלפים לנתונים המתאימים ל-Firebase
    private ArrayList<CardData> serializeCards(ArrayList<Card> cards) {
        ArrayList<CardData> cardDataList = new ArrayList<>();
        for (Card card : cards) {
            cardDataList.add(new CardData(card.getCatagory(), card.getCardName(), card.getId()));
        }
        return cardDataList;
    }

    // מחלקת עזר לשמירת קלפים ב-Firebase
    private static class CardData {
        public String category;
        public String name;
        public int id;

        public CardData() {} // נדרש ל-Firebase

        public CardData(String category, String name, int id) {
            this.category = category;
            this.name = name;
            this.id = id;
        }



    }
    public void switchTurn() {
        gameStateRef.child("currentTurn").get().addOnSuccessListener(snapshot -> {
            String currentTurn = snapshot.getValue(String.class);
            if (currentTurn != null) {
                String nextTurn = currentTurn.equals("player1") ? "player2" : "player1";
                updateTurn(nextTurn);
            }
        }).addOnFailureListener(e -> {
            Log.e("FbModule", "שגיאה במעבר תור: " + e.getMessage());
        });
    }

    // Add this method to FbModule.java

    // Add this method to update the background color
    public void updateBackgroundColor(String color) {
        gameStateRef.child("backgroundColor").setValue(color);
    }

    // Add this listener to listen for background color changes
    private void setupBackgroundColorListener() {
        gameStateRef.child("backgroundColor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String color = snapshot.getValue(String.class);
                if (color != null && gameStateListener != null) {
                    gameStateListener.onBackgroundColorChanged(color);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }}
