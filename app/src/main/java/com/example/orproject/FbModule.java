package com.example.orproject;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                            // Manual field extraction
                            String category = cardSnapshot.child("category").getValue(String.class);
                            String name = cardSnapshot.child("name").getValue(String.class);
                            Integer id = cardSnapshot.child("id").getValue(Integer.class);

                            if (category != null && name != null && id != null) {
                                cards.add(new Card(category, name, id));
                            }
                        }

                        Log.d("FbModule", "Player1: Updating player1 cards, count: " + cards.size());
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
                            // Manual field extraction
                            String category = cardSnapshot.child("category").getValue(String.class);
                            String name = cardSnapshot.child("name").getValue(String.class);
                            Integer id = cardSnapshot.child("id").getValue(Integer.class);

                            if (category != null && name != null && id != null) {
                                cards.add(new Card(category, name, id));
                            }
                        }

                        Log.d("FbModule", "Player1: Updating player2 cards, count: " + cards.size());
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
                            // Manual field extraction
                            String category = cardSnapshot.child("category").getValue(String.class);
                            String name = cardSnapshot.child("name").getValue(String.class);
                            Integer id = cardSnapshot.child("id").getValue(Integer.class);

                            if (category != null && name != null && id != null) {
                                cards.add(new Card(category, name, id));
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
                            // Manual field extraction
                            String category = cardSnapshot.child("category").getValue(String.class);
                            String name = cardSnapshot.child("name").getValue(String.class);
                            Integer id = cardSnapshot.child("id").getValue(Integer.class);

                            if (category != null && name != null && id != null) {
                                cards.add(new Card(category, name, id));
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
                            // Manual field extraction
                            String category = cardSnapshot.child("category").getValue(String.class);
                            String name = cardSnapshot.child("name").getValue(String.class);
                            Integer id = cardSnapshot.child("id").getValue(Integer.class);

                            if (category != null && name != null && id != null) {
                                cards.add(new Card(category, name, id));
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
                            // Manual field extraction
                            String category = cardSnapshot.child("category").getValue(String.class);
                            String name = cardSnapshot.child("name").getValue(String.class);
                            Integer id = cardSnapshot.child("id").getValue(Integer.class);

                            if (category != null && name != null && id != null) {
                                cards.add(new Card(category, name, id));
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

    // מעבר קלף לשחקן - שיטה משופרת
    public void moveCardToPlayer(String player, Card card) {
        try {
            Log.d("FbModule", "Moving card " + card.getCardName() + " to " + player);

            // Get the reference to the player's cards
            DatabaseReference playerCardsRef = gameStateRef.child(player + "Cards");

            // First, get the current cards
            playerCardsRef.get().addOnSuccessListener(snapshot -> {
                try {
                    ArrayList<Card> cards = new ArrayList<>();

                    // Parse existing cards with manual field extraction
                    for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                        String category = cardSnapshot.child("category").getValue(String.class);
                        String name = cardSnapshot.child("name").getValue(String.class);
                        Integer id = cardSnapshot.child("id").getValue(Integer.class);

                        if (category != null && name != null && id != null) {
                            cards.add(new Card(category, name, id));
                        }
                    }

                    // Add the new card
                    cards.add(card);

                    // Update immediately with the new list
                    playerCardsRef.setValue(serializeCards(cards))
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FbModule", "Successfully moved card to " + player);

                                // Check for quartet in the receiving player's hand
                                checkAndRemoveQuartet(player, cards);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FbModule", "Failed to move card: " + e.getMessage());
                            });

                } catch (Exception e) {
                    Log.e("FbModule", "Error processing cards while moving card: " + e.getMessage());
                }
            }).addOnFailureListener(e -> {
                Log.e("FbModule", "Failed to retrieve cards: " + e.getMessage());
            });
        } catch (Exception e) {
            Log.e("FbModule", "General error in moveCardToPlayer: " + e.getMessage());
        }
    }

    // New helper method to check for and remove a quartet in the receiving player's hand
    private void checkAndRemoveQuartet(String player, ArrayList<Card> cards) {
        // Check if there's a quartet
        HashMap<String, Integer> categoryCount = new HashMap<>();
        for (Card card : cards) {
            String category = card.getCatagory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }

        // Find a category with 4 or more cards
        String quartetCategory = null;
        for (String category : categoryCount.keySet()) {
            if (categoryCount.get(category) >= 4) {
                quartetCategory = category;
                break;
            }
        }

        // If a quartet is found, remove it and update score
        if (quartetCategory != null) {
            final String category = quartetCategory;

            // Find cards to remove
            ArrayList<Card> cardsToRemove = new ArrayList<>();
            int count = 0;

            for (Card card : cards) {
                if (card.getCatagory().equals(category) && count < 4) {
                    cardsToRemove.add(card);
                    count++;
                }

                if (count == 4) break;
            }

            // Remove the quartet cards
            cards.removeAll(cardsToRemove);

            // Update the player's cards
            gameStateRef.child(player + "Cards").setValue(serializeCards(cards));

            // Update the player's score
            gameStateRef.child("playerScores").child(player).get().addOnSuccessListener(snapshot -> {
                Integer currentScore = snapshot.getValue(Integer.class);
                if (currentScore == null) currentScore = 0;

                // Add 40 points for the quartet
                gameStateRef.child("playerScores").child(player).setValue(currentScore + 40);
            });
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

    // החלפת המרת קלפים לפורמט של מסד הנתונים מפרטי למוגדר ציבורית
    public ArrayList<CardData> serializeCards(ArrayList<Card> cards) {
        ArrayList<CardData> cardDataList = new ArrayList<>();
        for (Card card : cards) {
            cardDataList.add(new CardData(card.getCatagory(), card.getCardName(), card.getId()));
        }
        return cardDataList;
    }

    // Method to reset Firebase data structures if needed
    public void resetCardStructures() {
        Log.d("FbModule", "Resetting card structures in Firebase");

        // Reset player cards with empty arrays
        gameStateRef.child("player1Cards").setValue(new ArrayList<>());
        gameStateRef.child("player2Cards").setValue(new ArrayList<>());

        // Reset scores
        gameStateRef.child("playerScores").child("player1").setValue(0);
        gameStateRef.child("playerScores").child("player2").setValue(0);

        // Reset turn (start with player 1)
        gameStateRef.child("currentTurn").setValue("player1");
    }

    // Add method to debug the card states
    public void debugCardStates() {
        Log.d("FbModule", "==== DEBUGGING CARD STATES ====");

        gameStateRef.child("player1Cards").get().addOnSuccessListener(snapshot -> {
            Log.d("FbModule", "Player 1 cards count: " + snapshot.getChildrenCount());
            for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                // Manual field extraction
                String category = cardSnapshot.child("category").getValue(String.class);
                String name = cardSnapshot.child("name").getValue(String.class);
                Integer id = cardSnapshot.child("id").getValue(Integer.class);

                if (category != null && name != null && id != null) {
                    Log.d("FbModule", "P1 Card: " + category + " - " + name + " (ID: " + id + ")");
                }
            }
        });

        gameStateRef.child("player2Cards").get().addOnSuccessListener(snapshot -> {
            Log.d("FbModule", "Player 2 cards count: " + snapshot.getChildrenCount());
            for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
                // Manual field extraction
                String category = cardSnapshot.child("category").getValue(String.class);
                String name = cardSnapshot.child("name").getValue(String.class);
                Integer id = cardSnapshot.child("id").getValue(Integer.class);

                if (category != null && name != null && id != null) {
                    Log.d("FbModule", "P2 Card: " + category + " - " + name + " (ID: " + id + ")");
                }
            }
        });

        gameStateRef.child("packet").get().addOnSuccessListener(snapshot -> {
            Log.d("FbModule", "Packet cards count: " + snapshot.getChildrenCount());
        });
    }

    // מחלקה לייצוג קלף במסד הנתונים - נשארת ללא שינוי
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