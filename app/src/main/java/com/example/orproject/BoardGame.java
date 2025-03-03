package com.example.orproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class BoardGame extends View {
    private boolean isPlayer1;
    private ArrayList<Card> myCards = new ArrayList<>();
    private ArrayList<Card> opponentCards = new ArrayList<>();
    private Packet packet = new Packet();
    private boolean isMyTurn = false;
    private ArrayList<Card> player1Cards;
    private ArrayList<Card> player2Cards;

    private int myScore = 0;
    private int opponentScore = 0;
    private TextView tvOpponentCards;
    private Paint backgroundPaint;
    private Card[] ListOfCards = new Card[36];
    private Deck deck;
    private FbModule fbModule;
    private static final int PACKET_X = 0;
    private static final int PACKET_Y = 0;
    private static final int PACKET_WIDTH = 260;
    private static final int PACKET_HEIGHT = 400;

    private int scrollOffset = 0;
    private boolean canScrollLeft = false;
    private boolean canScrollRight = false;
    private static final int SCROLL_BUTTON_WIDTH = 100;
    private static final int SCROLL_BUTTON_HEIGHT = 100;

    public BoardGame(Context context, boolean isPlayer1) {
        super(context);
        this.isPlayer1 = isPlayer1;
        player1Cards = new ArrayList<>();
        player2Cards = new ArrayList<>();
        this.fbModule = new FbModule(null);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#D6EAF8"));
        backgroundPaint.setStyle(Paint.Style.FILL);

        if (context instanceof GameActivity) {
            tvOpponentCards = ((GameActivity) context).findViewById(R.id.tvAgainst);
            updateOpponentCardCount();
        }

        initializeGame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            // Check if scroll buttons were touched
            if (isScrollLeftButtonTouched(touchX, touchY)) {
                scrollOffset = Math.max(0, scrollOffset - 280);
                invalidate();
                return true;
            } else if (isScrollRightButtonTouched(touchX, touchY)) {
                if (canScrollRight) {
                    scrollOffset += 280;
                    invalidate();
                }
                return true;
            }

            if (isMyTurn) {
                // Check if packet was touched
                if (isPacketTouched(touchX, touchY)) {
                    handlePacketTouch();
                    return true;
                }

                // Check if any card was touched (adjusting for scroll)
                for (Card card : myCards) {
                    if (card.isUserTouchMe((int) touchX + scrollOffset, (int) touchY)) {
                        handleCardTouch(card);
                        return true;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isScrollLeftButtonTouched(float x, float y) {
        return canScrollLeft && x >= 20 && x <= 20 + SCROLL_BUTTON_WIDTH &&
                y >= getHeight() - 450 - SCROLL_BUTTON_HEIGHT / 2 && y <= getHeight() - 450 + SCROLL_BUTTON_HEIGHT / 2;
    }

    private boolean isScrollRightButtonTouched(float x, float y) {
        return canScrollRight && x >= getWidth() - 20 - SCROLL_BUTTON_WIDTH && x <= getWidth() - 20 &&
                y >= getHeight() - 450 - SCROLL_BUTTON_HEIGHT / 2 && y <= getHeight() - 450 + SCROLL_BUTTON_HEIGHT / 2;
    }

    private boolean isPacketTouched(float x, float y) {
        int packetX = getWidth() / 2 - 130;
        int packetY = getHeight() / 2 - 200;
        return x >= packetX && x <= packetX + 260 &&
                y >= packetY && y <= packetY + 400;
    }

    private void handlePacketTouch() {
        if (!packet.isEmpty()) {
            Card drawnCard = packet.drawCard();
            if (drawnCard != null) {
                myCards.add(drawnCard);

                // Update Firebase with new player cards
                if (isPlayer1) {
                    fbModule.updatePlayer1Cards(myCards);
                } else {
                    fbModule.updatePlayer2Cards(myCards);
                }

                // Update Firebase with new packet after drawing
                fbModule.updatePacket(packet.getAllCards());

                // Check for Rummy before switching turns
                if (checkForQuartet(myCards)) {
                    removeQuartetFromHand(myCards);
                } else {
                    // Switch turn after drawing a card
                    fbModule.switchTurn();
                }

                // Update screen
                invalidate();

                // Check for end game condition
                checkEndGame();
            }
        }
    }

    private void handleCardTouch(Card card) {
        // Remove card from current player's hand
        myCards.remove(card);

        // Update current player's cards in Firebase
        if (isPlayer1) {
            fbModule.updatePlayer1Cards(myCards);
            // Add card to opponent's hand
            fbModule.moveCardToPlayer("player2", card);
        } else {
            fbModule.updatePlayer2Cards(myCards);
            // Add card to opponent's hand
            fbModule.moveCardToPlayer("player1", card);
        }

        // Switch turn
        fbModule.switchTurn();

        // Refresh screen
        invalidate();
    }

    private void initializeGame() {
        deck = new Deck();
        deck.AddForDeck(ListOfCards);
        deck.Shuffle(ListOfCards);

        // Initial card distribution
        ArrayList<Card> initialCards = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (isPlayer1) {
                initialCards.add(ListOfCards[i]);
            } else {
                initialCards.add(ListOfCards[i + 4]);
            }
        }
        myCards = initialCards;

        // Add remaining cards to packet
        ArrayList<Card> remainingCards = new ArrayList<>();
        for (int i = 8; i < ListOfCards.length; i++) {
            remainingCards.add(ListOfCards[i]);
        }
        packet.setCards(remainingCards);

        // Update Firebase
        if (isPlayer1) {
            fbModule.updatePlayer1Cards(myCards);
        } else {
            fbModule.updatePlayer2Cards(myCards);
        }
        fbModule.updatePacket(remainingCards);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        drawPacket(canvas);
        drawPlayerCards(canvas, myCards);
        drawOpponentInfo(canvas);
        drawScores(canvas);
        drawScrollButtons(canvas);

        if (isMyTurn) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(60);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Your Turn!", getWidth() / 2, 200, textPaint);
        }

        // Check for end game
        if (packet.isEmpty() && (myCards.isEmpty() || opponentCards.isEmpty())) {
            drawGameOver(canvas);
        }
    }

    private void drawScrollButtons(Canvas canvas) {
        Paint buttonPaint = new Paint();
        buttonPaint.setColor(Color.GRAY);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Calculate if scrolling is needed
        int totalWidth = myCards.size() * 280;
        int visibleWidth = getWidth();

        canScrollLeft = scrollOffset > 0;
        canScrollRight = totalWidth > visibleWidth + scrollOffset;

        // Draw left scroll button if needed
        if (canScrollLeft) {
            canvas.drawRect(20, getHeight() - 450 - SCROLL_BUTTON_HEIGHT/2,
                    20 + SCROLL_BUTTON_WIDTH, getHeight() - 450 + SCROLL_BUTTON_HEIGHT/2, buttonPaint);
            canvas.drawText("<", 20 + SCROLL_BUTTON_WIDTH/2, getHeight() - 450 + 15, textPaint);
        }

        // Draw right scroll button if needed
        if (canScrollRight) {
            canvas.drawRect(getWidth() - 20 - SCROLL_BUTTON_WIDTH, getHeight() - 450 - SCROLL_BUTTON_HEIGHT/2,
                    getWidth() - 20, getHeight() - 450 + SCROLL_BUTTON_HEIGHT/2, buttonPaint);
            canvas.drawText(">", getWidth() - 20 - SCROLL_BUTTON_WIDTH/2, getHeight() - 450 + 15, textPaint);
        }
    }

    private void drawGameOver(Canvas canvas) {
        Paint overlay = new Paint();
        overlay.setColor(Color.argb(180, 0, 0, 0)); // Semi-transparent black
        canvas.drawRect(0, 0, getWidth(), getHeight(), overlay);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setTextAlign(Paint.Align.CENTER);

        String winner;
        if (myScore > opponentScore) {
            winner = isPlayer1 ? "Player 1 Wins!" : "Player 2 Wins!";
        } else if (opponentScore > myScore) {
            winner = isPlayer1 ? "Player 2 Wins!" : "Player 1 Wins!";
        } else {
            winner = "It's a Tie!";
        }

        canvas.drawText("Game Over", getWidth()/2, getHeight()/2 - 100, textPaint);
        canvas.drawText(winner, getWidth()/2, getHeight()/2, textPaint);
        canvas.drawText("Score: " + myScore + " - " + opponentScore, getWidth()/2, getHeight()/2 + 100, textPaint);
    }

    private void drawScores(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.LEFT);

        String playerLabel = isPlayer1 ? "Player 1" : "Player 2";
        String opponentLabel = isPlayer1 ? "Player 2" : "Player 1";

        canvas.drawText(playerLabel + " Score: " + myScore, 50, 150, textPaint);
        canvas.drawText(opponentLabel + " Score: " + opponentScore, 50, 200, textPaint);
    }

    public void updateScore(int score) {
        opponentScore = score;
        invalidate();
    }

    private void drawPacket(Canvas canvas) {
        // Draw packet in red like the cards
        Paint packetPaint = new Paint();
        packetPaint.setColor(Color.RED);
        int packetX = getWidth() / 2 - 130;
        int packetY = getHeight() / 2 - 200;
        canvas.drawRect(packetX, packetY,
                packetX + 260, packetY + 400, packetPaint);

        // Add text "Packet" on the card and card count
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Packet", packetX + 130, packetY + 180, textPaint);
        canvas.drawText("(" + packet.size() + ")", packetX + 130, packetY + 240, textPaint);
    }

    private void drawPlayerCards(Canvas canvas, ArrayList<Card> cards) {
        int spacing = 280; // Space between cards
        int startX = (getWidth() - (cards.size() * spacing)) / 2;

        // Adjust for scrolling
        startX -= scrollOffset;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            card.setX(startX + (i * spacing));
            card.setY(getHeight() - 450);

            // Only draw cards that are visible on screen
            if (card.getX() + 260 >= 0 && card.getX() <= getWidth()) {
                card.draw(canvas);
            }
        }
    }

    private void drawOpponentInfo(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.LEFT);
        String opponentName = isPlayer1 ? "Player 2" : "Player 1";
        canvas.drawText(opponentName + "'s Cards: " + opponentCards.size(),
                50, 100, textPaint);
    }

    public void updatePlayerCards(ArrayList<Card> cards, boolean isPlayer1Update) {
        if (isPlayer1Update) {
            player1Cards = new ArrayList<>(cards);
            if (isPlayer1) {
                myCards.clear();
                myCards.addAll(player1Cards);
            } else {
                opponentCards.clear();
                opponentCards.addAll(player1Cards);
            }
        } else {
            player2Cards = new ArrayList<>(cards);
            if (!isPlayer1) {
                myCards.clear();
                myCards.addAll(player2Cards);
            } else {
                opponentCards.clear();
                opponentCards.addAll(player2Cards);
            }
        }

        updateOpponentCardCount();
        invalidate();
    }




    private void updateOpponentCardCount() {
        if (tvOpponentCards != null) {
            String opponentName = isPlayer1 ? "Player 2" : "Player 1";
            tvOpponentCards.setText(opponentName + "'s Cards: " + opponentCards.size());
        }
    }

    public void setTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        invalidate();
    }

    public void updatePacket(ArrayList<Card> cards) {
        packet.setCards(cards);
        invalidate();
    }

    /**
     * Check if the player has a quartet (4 cards of the same category)
     */
    public boolean checkForQuartet(ArrayList<Card> cards) {
        if (cards.size() < 4) {
            return false;
        }

        // Count cards by category
        java.util.HashMap<String, Integer> categoryCount = new java.util.HashMap<>();
        for (Card card : cards) {
            String category = card.getCatagory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }

        // Check if any category has 4 or more cards
        for (Integer count : categoryCount.values()) {
            if (count >= 4) {
                return true;
            }
        }

        return false;
    }

    /**
     * Remove a quartet from the player's hand and update score
     */
    public void removeQuartetFromHand(ArrayList<Card> cards) {
        if (!checkForQuartet(cards)) {
            return;
        }

        // Find the first category with 4 or more cards
        java.util.HashMap<String, Integer> categoryCount = new java.util.HashMap<>();
        for (Card card : cards) {
            String category = card.getCatagory();
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
        }

        String quartetCategory = null;
        for (String category : categoryCount.keySet()) {
            if (categoryCount.get(category) >= 4) {
                quartetCategory = category;
                break;
            }
        }

        if (quartetCategory != null) {
            // Find and remove exactly 4 cards of the quartet category
            ArrayList<Card> cardsToRemove = new ArrayList<>();
            int count = 0;

            for (Card card : cards) {
                if (card.getCatagory().equals(quartetCategory) && count < 4) {
                    cardsToRemove.add(card);
                    count++;
                }

                if (count == 4) {
                    break;
                }
            }

            // Remove the cards
            cards.removeAll(cardsToRemove);

            // Update score
            myScore += 40;

            // Update Firebase
            if (isPlayer1) {
                fbModule.updatePlayerScore("player1", myScore);
                fbModule.updatePlayer1Cards(cards);
            } else {
                fbModule.updatePlayerScore("player2", myScore);
                fbModule.updatePlayer2Cards(cards);
            }

            // Check for end game or another quartet
            if (cards.isEmpty() || cards.size() < 4) {
                checkEndGame();
            } else if (checkForQuartet(cards)) {
                // Handle another quartet if present
                removeQuartetFromHand(cards);
            }

            invalidate();
        }
    }

    private void checkEndGame() {
        // Game ends when the packet is empty and at least one player has no cards left
        // OR when both players have no more cards to play
        if ((packet.isEmpty() && (myCards.isEmpty() || opponentCards.isEmpty())) ||
                (myCards.isEmpty() && opponentCards.isEmpty())) {

            // Determine the winner
            String winner;
            int winnerScore;
            if (myScore > opponentScore) {
                winner = isPlayer1 ? "Player 1" : "Player 2";
                winnerScore = myScore;
            } else if (opponentScore > myScore) {
                winner = isPlayer1 ? "Player 2" : "Player 1";
                winnerScore = opponentScore;
            } else {
                winner = "It's a Tie";
                winnerScore = myScore; // Both scores are equal
            }

            // Show custom dialog with the winner
            if (getContext() != null) {
                CustomDialog customDialog = new CustomDialog(getContext());
                customDialog.showWinnerDialog(winner, winnerScore);
            }

            // Update the UI
            invalidate();
        }
    }
}