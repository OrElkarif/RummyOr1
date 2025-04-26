package com.example.orproject;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FbModule {
    // Firebase database
    private final FirebaseDatabase database;
    // Reference to game state
    public final DatabaseReference gameStateRef;
    // Context of the activity
    private final Context context;
    // Firebase Authentication
    private final FirebaseAuth mAuth;
    // User Stats Reference
    private DatabaseReference userStatsRef;
    // User Statistics
    private int completedQuartets = 0;
    private int gamesPlayed = 0;
    private int totalScore = 0;

    private static final String TAG = "FbModule";

    /**
     * Constructor - initializes Firebase connections and listeners
     */
    public FbModule(Context context) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        gameStateRef = database.getReference("gameState");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userStatsRef = database.getReference("userStats").child(currentUser.getUid());
            loadUserStats();
        }

        if (context != null) {
            setupListeners();
        }
    }

    /**
     * Load current user statistics from Firebase
     */
    private void loadUserStats() {
        if (userStatsRef == null) return;

        userStatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer quartets = dataSnapshot.child("completedQuartets").getValue(Integer.class);
                    Integer games = dataSnapshot.child("gamesPlayed").getValue(Integer.class);
                    Integer score = dataSnapshot.child("totalScore").getValue(Integer.class);

                    if (quartets != null) completedQuartets = quartets;
                    if (games != null) gamesPlayed = games;
                    if (score != null) totalScore = score;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user stats: " + error.getMessage());
            }
        });
    }

    /**
     * Update user statistics when a quartet is completed
     */
    public void updateUserQuartetStats() {
        if (userStatsRef == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("completedQuartets", ++completedQuartets);
        updates.put("totalScore", totalScore += 40);

        userStatsRef.updateChildren(updates);
    }

    /**
     * Increment games played counter
     */
    public void incrementGamesPlayed() {
        if (userStatsRef == null) return;
        userStatsRef.child("gamesPlayed").setValue(++gamesPlayed);
    }

    /**
     * Set up all listeners
     */
    private void setupListeners() {
        setupBackgroundColorListener();

        if (context instanceof Player1Activity) {
            setupPlayer1Listeners();
        } else if (context instanceof Player2Activity) {
            setupPlayer2Listeners();
        }
    }

    /**
     * Set up background color listener
     */
    private void setupBackgroundColorListener() {
        try {
            gameStateRef.child("backgroundColor").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (context == null) return;

                    String color = snapshot.getValue(String.class);
                    if (color != null) {
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
                    Log.e(TAG, "Background color error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Background listener error: " + e.getMessage());
        }
    }

    /**
     * Set up Player 1 listeners
     */
    private void setupPlayer1Listeners() {
        try {
            // Player 1 cards listener
            gameStateRef.child("player1Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = parseCardsFromSnapshot(snapshot);
                        ((Player1Activity) context).boardGame.updatePlayerCards(cards, true);
                    } catch (Exception e) {
                        Log.e(TAG, "Player1 cards error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Player1 cards cancelled: " + error.getMessage());
                }
            });

            // Player 2 cards listener
            gameStateRef.child("player2Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ArrayList<Card> cards = parseCardsFromSnapshot(snapshot);
                        ((Player1Activity) context).boardGame.updatePlayerCards(cards, false);
                    } catch (Exception e) {
                        Log.e(TAG, "Player2 cards error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Player2 cards cancelled: " + error.getMessage());
                }
            });

            // Packet listener
            gameStateRef.child("packet").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ((Player1Activity) context).boardGame.updatePacket(parseCardsFromSnapshot(snapshot));
                    } catch (Exception e) {
                        Log.e(TAG, "Packet error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Packet cancelled: " + error.getMessage());
                }
            });

            // Turn listener
            gameStateRef.child("currentTurn").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String currentPlayer = snapshot.getValue(String.class);
                        if (currentPlayer != null) {
                            ((Player1Activity) context).boardGame.setTurn(currentPlayer.equals("player1"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Turn error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Turn cancelled: " + error.getMessage());
                }
            });

            // Score listener
            gameStateRef.child("playerScores").child("player2").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Integer score = snapshot.getValue(Integer.class);
                        if (score != null) {
                            ((Player1Activity) context).boardGame.updateScore(score);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Score error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Score cancelled: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Player1 listeners error: " + e.getMessage());
        }
    }

    /**
     * Set up Player 2 listeners
     */
    private void setupPlayer2Listeners() {
        try {
            // Player 1 cards listener
            gameStateRef.child("player1Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ((Player2Activity) context).boardGame.updatePlayerCards(parseCardsFromSnapshot(snapshot), true);
                    } catch (Exception e) {
                        Log.e(TAG, "Player1 cards error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Player1 cards cancelled: " + error.getMessage());
                }
            });

            // Player 2 cards listener
            gameStateRef.child("player2Cards").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ((Player2Activity) context).boardGame.updatePlayerCards(parseCardsFromSnapshot(snapshot), false);
                    } catch (Exception e) {
                        Log.e(TAG, "Player2 cards error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Player2 cards cancelled: " + error.getMessage());
                }
            });

            // Packet listener
            gameStateRef.child("packet").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        ((Player2Activity) context).boardGame.updatePacket(parseCardsFromSnapshot(snapshot));
                    } catch (Exception e) {
                        Log.e(TAG, "Packet error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Packet cancelled: " + error.getMessage());
                }
            });

            // Turn listener
            gameStateRef.child("currentTurn").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        String currentPlayer = snapshot.getValue(String.class);
                        if (currentPlayer != null) {
                            ((Player2Activity) context).boardGame.setTurn(currentPlayer.equals("player2"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Turn error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Turn cancelled: " + error.getMessage());
                }
            });

            // Score listener
            gameStateRef.child("playerScores").child("player1").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Integer score = snapshot.getValue(Integer.class);
                        if (score != null) {
                            ((Player2Activity) context).boardGame.updateScore(score);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Score error: " + e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Score cancelled: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Player2 listeners error: " + e.getMessage());
        }
    }

    /**
     * Helper method to parse cards from a Firebase DataSnapshot
     */
    private ArrayList<Card> parseCardsFromSnapshot(DataSnapshot snapshot) {
        ArrayList<Card> cards = new ArrayList<>();
        for (DataSnapshot cardSnapshot : snapshot.getChildren()) {
            String category = cardSnapshot.child("category").getValue(String.class);
            String name = cardSnapshot.child("name").getValue(String.class);
            Integer id = cardSnapshot.child("id").getValue(Integer.class);

            if (category != null && name != null && id != null) {
                cards.add(new Card(category, name, id));
            }
        }
        return cards;
    }

    /**
     * Update Player 1 cards
     */
    public void updatePlayer1Cards(ArrayList<Card> cards) {
        try {
            gameStateRef.child("player1Cards").setValue(serializeCards(cards));
        } catch (Exception e) {
            Log.e(TAG, "Update player1 cards error: " + e.getMessage());
        }
    }

    /**
     * Update Player 2 cards
     */
    public void updatePlayer2Cards(ArrayList<Card> cards) {
        try {
            gameStateRef.child("player2Cards").setValue(serializeCards(cards));
        } catch (Exception e) {
            Log.e(TAG, "Update player2 cards error: " + e.getMessage());
        }
    }

    /**
     * Update packet
     */
    public void updatePacket(ArrayList<Card> cards) {
        try {
            gameStateRef.child("packet").setValue(serializeCards(cards));
        } catch (Exception e) {
            Log.e(TAG, "Update packet error: " + e.getMessage());
        }
    }

    /**
     * Update current turn
     */
    public static void updateTurn(String player) {
        try {
            FirebaseDatabase.getInstance().getReference("gameState").child("currentTurn").setValue(player);
        } catch (Exception e) {
            Log.e("FbModule", "Update turn error: " + e.getMessage());
        }
    }

    /**
     * Update player score
     */
    public void updatePlayerScore(String player, int score) {
        try {
            gameStateRef.child("playerScores").child(player).setValue(score)
                    .addOnSuccessListener(aVoid -> {
                        if (userStatsRef != null) {
                            totalScore = score;
                            userStatsRef.child("totalScore").setValue(totalScore);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Update score error: " + e.getMessage());
        }
    }

    /**
     * Move card to player
     */
    public void moveCardToPlayer(String player, Card card) {
        try {
            DatabaseReference playerCardsRef = gameStateRef.child(player + "Cards");

            playerCardsRef.get().addOnSuccessListener(snapshot -> {
                ArrayList<Card> cards = parseCardsFromSnapshot(snapshot);
                cards.add(card);

                playerCardsRef.setValue(serializeCards(cards))
                        .addOnSuccessListener(aVoid -> checkAndRemoveQuartet(player, cards));
            });
        } catch (Exception e) {
            Log.e(TAG, "Move card error: " + e.getMessage());
        }
    }

    /**
     * Check for and remove a quartet
     */
    private void checkAndRemoveQuartet(String player, ArrayList<Card> cards) {
        HashMap<String, Integer> categoryCount = new HashMap<>();
        for (Card card : cards) {
            categoryCount.put(card.getCatagory(), categoryCount.getOrDefault(card.getCatagory(), 0) + 1);
        }

        String quartetCategory = null;
        for (Map.Entry<String, Integer> entry : categoryCount.entrySet()) {
            if (entry.getValue() >= 4) {
                quartetCategory = entry.getKey();
                break;
            }
        }

        if (quartetCategory == null) return;

        final String category = quartetCategory;
        ArrayList<Card> cardsToRemove = new ArrayList<>();
        int count = 0;

        for (Card card : cards) {
            if (card.getCatagory().equals(category) && count < 4) {
                cardsToRemove.add(card);
                count++;
                if (count == 4) break;
            }
        }

        cards.removeAll(cardsToRemove);
        gameStateRef.child(player + "Cards").setValue(serializeCards(cards));

        gameStateRef.child("playerScores").child(player).get().addOnSuccessListener(snapshot -> {
            Integer currentScore = snapshot.getValue(Integer.class);
            if (currentScore == null) currentScore = 0;
            gameStateRef.child("playerScores").child(player).setValue(currentScore + 40);

            // Update user stats if this is the current player
            updateUserQuartetStats();
        });
    }

    /**
     * Update background color
     */
    public void updateBackgroundColor(String color) {
        try {
            gameStateRef.child("backgroundColor").setValue(color);
        } catch (Exception e) {
            Log.e(TAG, "Update color error: " + e.getMessage());
        }
    }

    /**
     * Switch turn
     */
    public void switchTurn() {
        try {
            gameStateRef.child("currentTurn").get().addOnSuccessListener(snapshot -> {
                String currentTurn = snapshot.getValue(String.class);
                if (currentTurn != null) {
                    updateTurn(currentTurn.equals("player1") ? "player2" : "player1");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Switch turn error: " + e.getMessage());
        }
    }

    /**
     * Reset game
     */
    public void resetGame() {
        try {
            gameStateRef.child("player1Cards").setValue(new ArrayList<>());
            gameStateRef.child("player2Cards").setValue(new ArrayList<>());
            gameStateRef.child("packet").setValue(new ArrayList<>());
            gameStateRef.child("playerScores").child("player1").setValue(0);
            gameStateRef.child("playerScores").child("player2").setValue(0);

            gameStateRef.child("backgroundColor").get().addOnSuccessListener(snapshot -> {
                if (snapshot.getValue() == null) {
                    gameStateRef.child("backgroundColor").setValue("Blue");
                }
            });

            // Increment games played for the user
            incrementGamesPlayed();
        } catch (Exception e) {
            Log.e(TAG, "Reset game error: " + e.getMessage());
        }
    }

    /**
     * Convert cards to Firebase format
     */
    public ArrayList<CardData> serializeCards(ArrayList<Card> cards) {
        ArrayList<CardData> cardDataList = new ArrayList<>();
        for (Card card : cards) {
            cardDataList.add(new CardData(card.getCatagory(), card.getCardName(), card.getId()));
        }
        return cardDataList;
    }

    /**
     * Debug card states (used only in development)
     */
    public void debugCardStates() {
        gameStateRef.child("player1Cards").get().addOnSuccessListener(snapshot -> {
            Log.d(TAG, "Player 1 cards count: " + snapshot.getChildrenCount());
        });

        gameStateRef.child("player2Cards").get().addOnSuccessListener(snapshot -> {
            Log.d(TAG, "Player 2 cards count: " + snapshot.getChildrenCount());
        });

        gameStateRef.child("packet").get().addOnSuccessListener(snapshot -> {
            Log.d(TAG, "Packet cards count: " + snapshot.getChildrenCount());
        });
    }

    /**
     * Card data class for Firebase
     */

}