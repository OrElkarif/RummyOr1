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
    // מסד הנתונים של פיירבייס
    FirebaseDatabase database;
    // הפניה למסד הנתונים
    DatabaseReference gameStateRef;
    // קונטקסט של האקטיביטי
    Context context;

    // בנאי המחלקה
    public FbModule(Context context) {
        this.context = context;
        // איתחול מסד הנתונים
        database = FirebaseDatabase.getInstance();
        // יצירת הפניה לצומת gameState
        gameStateRef = database.getReference("gameState");

        // הגדרת מאזינים רק אם יש קונטקסט
        if (context != null) {
            setupListeners();
        }
    }

    // הגדרת כל המאזינים
    private void setupListeners() {
        // מאזין לצבע רקע
        setupBackgroundColorListener();

        // הוספת מאזינים נוספים לפי סוג האקטיביטי
        if (context instanceof Player1Activity) {
            setupPlayer1Listeners();
        } else if (context instanceof Player2Activity) {
            setupPlayer2Listeners();
        }
    }

    // מאזין לצבע רקע
    private void setupBackgroundColorListener() {
        try {
            gameStateRef.child("backgroundColor").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (context == null) return;

                    String color = snapshot.getValue(String.class);
                    if (color != null) {
                        // עדכון צבע הרקע לפי סוג האקטיביטי
                        if (context instanceof Player1Activity) {
                            ((Player1Activity) context).setBackgroundColor(color);
                        } else if (context instanceof Player2Activity) {
                            ((Player2Activity) context).setBackgroundColor(color);
                        } else if (context instanceof GameActivity) {
                            ((GameActivity) context).setBackgroundColor(color);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת צבע רקע: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה בהגדרת מאזין לצבע רקע: " + e.getMessage());
        }
    }

    // מאזינים לשחקן 1
    private void setupPlayer1Listeners() {
        try {
            // מאזין לקלפים של שחקן 1
            gameStateRef.child("player1Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = new ArrayList<>();

                        for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                            CardData cardData = cardSnapshot.getValue(CardData.class);
                            if (cardData != null) {
                                cards.add(new Card(cardData.category, cardData.name, cardData.id));
                            }
                        }

                        ((Player1Activity) context).boardGame.updatePlayerCards(cards, true);
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד קלפי שחקן 1: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת קלפי שחקן 1: " + error.getMessage());
                }
            });

            // מאזין לקלפים של שחקן 2
            gameStateRef.child("player2Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = new ArrayList<>();

                        for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                            CardData cardData = cardSnapshot.getValue(CardData.class);
                            if (cardData != null) {
                                cards.add(new Card(cardData.category, cardData.name, cardData.id));
                            }
                        }

                        ((Player1Activity) context).boardGame.updatePlayerCards(cards, false);
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד קלפי שחקן 2: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת קלפי שחקן 2: " + error.getMessage());
                }
            });

            // מאזין לקופה
            gameStateRef.child("packet").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = new ArrayList<>();

                        for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                            CardData cardData = cardSnapshot.getValue(CardData.class);
                            if (cardData != null) {
                                cards.add(new Card(cardData.category, cardData.name, cardData.id));
                            }
                        }

                        ((Player1Activity) context).boardGame.updatePacket(cards);
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד קלפי הקופה: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת קלפי הקופה: " + error.getMessage());
                }
            });

            // מאזין לתור
            gameStateRef.child("currentTurn").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String currentPlayer = snapshot.getValue(String.class);
                        if (currentPlayer != null) {
                            ((Player1Activity) context).boardGame.setTurn(currentPlayer.equals("player1"));
                        }
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד התור הנוכחי: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת התור הנוכחי: " + error.getMessage());
                }
            });

            // מאזין לניקוד
            gameStateRef.child("playerScores").child("player2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Integer score = snapshot.getValue(Integer.class);
                        if (score != null) {
                            ((Player1Activity) context).boardGame.updateScore(score);
                        }
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד ניקוד: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת ניקוד: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה כללית במאזינים לשחקן 1: " + e.getMessage());
        }
    }

    // מאזינים לשחקן 2
    private void setupPlayer2Listeners() {
        try {
            // מאזין לקלפים של שחקן 1
            gameStateRef.child("player1Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = new ArrayList<>();

                        for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                            CardData cardData = cardSnapshot.getValue(CardData.class);
                            if (cardData != null) {
                                cards.add(new Card(cardData.category, cardData.name, cardData.id));
                            }
                        }

                        ((Player2Activity) context).boardGame.updatePlayerCards(cards, true);
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד קלפי שחקן 1: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת קלפי שחקן 1: " + error.getMessage());
                }
            });

            // מאזין לקלפים של שחקן 2
            gameStateRef.child("player2Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = new ArrayList<>();

                        for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                            CardData cardData = cardSnapshot.getValue(CardData.class);
                            if (cardData != null) {
                                cards.add(new Card(cardData.category, cardData.name, cardData.id));
                            }
                        }

                        ((Player2Activity) context).boardGame.updatePlayerCards(cards, false);
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד קלפי שחקן 2: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת קלפי שחקן 2: " + error.getMessage());
                }
            });

            // מאזין לקופה
            gameStateRef.child("packet").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = new ArrayList<>();

                        for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                            CardData cardData = cardSnapshot.getValue(CardData.class);
                            if (cardData != null) {
                                cards.add(new Card(cardData.category, cardData.name, cardData.id));
                            }
                        }

                        ((Player2Activity) context).boardGame.updatePacket(cards);
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד קלפי הקופה: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת קלפי הקופה: " + error.getMessage());
                }
            });

            // מאזין לתור
            gameStateRef.child("currentTurn").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String currentPlayer = snapshot.getValue(String.class);
                        if (currentPlayer != null) {
                            ((Player2Activity) context).boardGame.setTurn(currentPlayer.equals("player2"));
                        }
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד התור הנוכחי: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת התור הנוכחי: " + error.getMessage());
                }
            });

            // מאזין לניקוד
            gameStateRef.child("playerScores").child("player1").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Integer score = snapshot.getValue(Integer.class);
                        if (score != null) {
                            ((Player2Activity) context).boardGame.updateScore(score);
                        }
                    } catch (Exception e) {
                        Log.e("FbModule", "שגיאה בעיבוד ניקוד: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FbModule", "שגיאה בקריאת ניקוד: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה כללית במאזינים לשחקן 2: " + e.getMessage());
        }
    }

    // עדכון קלפי שחקן 1
    public void updatePlayer1Cards(ArrayList<Card> cards) {
        try {
            gameStateRef.child("player1Cards").setValue(serializeCards(cards));
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה בעדכון קלפי שחקן 1: " + e.getMessage());
        }
    }

    // עדכון קלפי שחקן 2
    public void updatePlayer2Cards(ArrayList<Card> cards) {
        try {
            gameStateRef.child("player2Cards").setValue(serializeCards(cards));
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה בעדכון קלפי שחקן 2: " + e.getMessage());
        }
    }

    // עדכון קופה
    public void updatePacket(ArrayList<Card> cards) {
        try {
            gameStateRef.child("packet").setValue(serializeCards(cards));
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה בעדכון הקופה: " + e.getMessage());
        }
    }

    // עדכון התור הנוכחי
    public static void updateTurn(String player) {
        try {
            FirebaseDatabase.getInstance().getReference("gameState").child("currentTurn").setValue(player);
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה בעדכון התור: " + e.getMessage());
        }
    }

    // עדכון ניקוד שחקן
    public void updatePlayerScore(String player, int score) {
        try {
            gameStateRef.child("playerScores").child(player).setValue(score);
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה בעדכון ניקוד: " + e.getMessage());
        }
    }

    // מעבר קלף לשחקן
    public void moveCardToPlayer(String player, Card card) {
        try {
            gameStateRef.child(player + "Cards").get().addOnSuccessListener(snapshot -> {
                try {
                    ArrayList<Card> cards = new ArrayList<>();

                    for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                        CardData cardData = cardSnapshot.getValue(CardData.class);
                        if (cardData != null) {
                            cards.add(new Card(cardData.category, cardData.name, cardData.id));
                        }
                    }

                    cards.add(card);
                    gameStateRef.child(player + "Cards").setValue(serializeCards(cards));
                } catch (Exception e) {
                    Log.e("FbModule", "שגיאה בהעברת קלף לשחקן: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה כללית בהעברת קלף לשחקן: " + e.getMessage());
        }
    }

    // עדכון צבע רקע
    public void updateBackgroundColor(String color) {
        try {
            gameStateRef.child("backgroundColor").setValue(color);
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה בעדכון צבע רקע: " + e.getMessage());
        }
    }

    // החלפת תור
    public void switchTurn() {
        try {
            gameStateRef.child("currentTurn").get().addOnSuccessListener(snapshot -> {
                try {
                    String currentTurn = snapshot.getValue(String.class);
                    if (currentTurn != null) {
                        String nextTurn = currentTurn.equals("player1") ? "player2" : "player1";
                        updateTurn(nextTurn);
                    }
                } catch (Exception e) {
                    Log.e("FbModule", "שגיאה בהחלפת תור: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה כללית בהחלפת תור: " + e.getMessage());
        }
    }

    // איפוס המשחק
    public void resetGame() {
        try {
            // במקום למחוק את כל הצומת, נאתחל את הערכים לריקים
            gameStateRef.child("player1Cards").setValue(new ArrayList<>());
            gameStateRef.child("player2Cards").setValue(new ArrayList<>());
            gameStateRef.child("packet").setValue(new ArrayList<>());
            gameStateRef.child("playerScores").child("player1").setValue(0);
            gameStateRef.child("playerScores").child("player2").setValue(0);

            // שימור צבע הרקע
            gameStateRef.child("backgroundColor").get().addOnSuccessListener(snapshot -> {
                String color = snapshot.getValue(String.class);
                if (color == null) {
                    gameStateRef.child("backgroundColor").setValue("Blue");
                }
            });
        } catch (Exception e) {
            Log.e("FbModule", "שגיאה באיפוס המשחק: " + e.getMessage());
        }
    }

    // המרת קלפים לפורמט של מסד הנתונים
    private ArrayList<CardData> serializeCards(ArrayList<Card> cards) {
        ArrayList<CardData> cardDataList = new ArrayList<>();
        for (Card card : cards) {
            cardDataList.add(new CardData(card.getCatagory(), card.getCardName(), card.getId()));
        }
        return cardDataList;
    }

    // מחלקה לייצוג קלף במסד הנתונים
    public class CardData {
        public String category;
        public String name;
        public int id;

        public CardData() {
            // בנאי ריק הנדרש לפיירבייס
        }

        public CardData(String category, String name, int id) {
            this.category = category;
            this.name = name;
            this.id = id;
        }
    }
}