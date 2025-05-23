package com.example.orproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.GenericTypeIndicator;
import java.util.HashMap;
import java.util.Map;

public class BoardGame extends View { // הגדרת המחלקה BoardGame שיורשת מ-View
    // משתני מצב המשחק
    private boolean isPlayer1; // משתנה בוליאני המציין אם זה שחקן 1 (true) או שחקן 2 (false)
    private ArrayList<Card> myCards = new ArrayList<>(); // מערך של הקלפים של השחקן הנוכחי
    private ArrayList<Card> opponentCards = new ArrayList<>(); // מערך של הקלפים של היריב
    private ArrayList<Card> player1Cards = new ArrayList<>(); // מערך של הקלפים של שחקן 1
    private ArrayList<Card> player2Cards = new ArrayList<>(); // מערך של הקלפים של שחקן 2
    private Packet packet = new Packet(); // אובייקט המייצג את קופת הקלפים
    private boolean isMyTurn = false; // משתנה בוליאני המציין אם זה התור של השחקן הנוכחי

    // משתני ניקוד וממשק
    private int myScore = 0; // ניקוד השחקן הנוכחי
    private int opponentScore = 0; // ניקוד היריב
    private TextView tvOpponentCards; // אובייקט TextView להצגת מספר הקלפים של היריב
    private Paint backgroundPaint; // אובייקט Paint לצביעת הרקע
    private Card[] ListOfCards = new Card[36]; // מערך של כל הקלפים במשחק - 36 קלפים
    private Deck deck; // אובייקט המייצג את החפיסה
    private FbModule fbModule; // אובייקט המייצג את מודול ה-Firebase

    // קבועים למיקום וגודל הקופה
    private static final int PACKET_X = 0; // מיקום X של הקופה
    private static final int PACKET_Y = 0; // מיקום Y של הקופה
    private static final int PACKET_WIDTH = 260; // רוחב הקופה
    private static final int PACKET_HEIGHT = 400; // גובה הקופה

    // משתני גלילה עבור הקלפים
    private int scrollOffset = 0; // משתנה המייצג את ההיסט בגלילה
    private boolean canScrollLeft = false; // משתנה בוליאני המציין אם ניתן לגלול שמאלה
    private boolean canScrollRight = false; // משתנה בוליאני המציין אם ניתן לגלול ימינה
    private static final int SCROLL_BUTTON_WIDTH = 100; // רוחב כפתור הגלילה
    private static final int SCROLL_BUTTON_HEIGHT = 100; // גובה כפתור הגלילה

    // מיקום כפתור "I don't have"
    private int iDontHaveButtonX; // מיקום X של כפתור "אין לי"
    private int iDontHaveButtonY; // מיקום Y של כפתור "אין לי"
    private int iDontHaveButtonWidth = 250; // רוחב כפתור "אין לי"
    private int iDontHaveButtonHeight = 100; // גובה כפתור "אין לי"

    // בנאי המחלקה - מאתחל את לוח המשחק
    public BoardGame(Context context, boolean isPlayer1) { // הבנאי מקבל קונטקסט ומשתנה המציין אם זה שחקן 1
        super(context); // קריאה לבנאי של המחלקה הבסיסית View
        this.isPlayer1 = isPlayer1; // שמירת המשתנה המציין אם זה שחקן 1
        this.fbModule = new FbModule(context); // יצירת אובייקט של מודול ה-Firebase

        // הגדרת רקע כחול בהיר
        backgroundPaint = new Paint(); // יצירת אובייקט Paint
        backgroundPaint.setColor(Color.parseColor("#D6EAF8")); // הגדרת צבע הרקע לכחול בהיר
        backgroundPaint.setStyle(Paint.Style.FILL); // הגדרת סגנון הציור למילוי

        // חיבור ל-TextView של קלפי היריב
        if (context instanceof GameActivity) { // בדיקה אם הקונטקסט הוא מסוג GameActivity
            tvOpponentCards = ((GameActivity) context).findViewById(R.id.tvAgainst); // מציאת ה-TextView ושמירה במשתנה
            updateOpponentCardCount(); // עדכון מספר הקלפים של היריב
        }

        // אתחול המשחק
        initializeGame(); // קריאה לפונקציה שמאתחלת את המשחק
    }
    private void directCardTouch(Card card) {
        try {
            Log.d("BoardGame", "Moving card: " + card.getCatagory() + " - " + card.getCardName());

            // הסר את הקלף מהיד של השחקן הנוכחי
            myCards.remove(card);

            // העבר את הקלף לשחקן השני באמצעות הפונקציה הקיימת
            if (isPlayer1) {
                fbModule.updatePlayer1Cards(myCards);
                fbModule.moveCardToPlayer("player2", card);
            } else {
                fbModule.updatePlayer2Cards(myCards);
                fbModule.moveCardToPlayer("player1", card);
            }

            // החלף תור
            fbModule.switchTurn();

            // רענן את המסך
            invalidate();

        } catch (Exception e) {
            Log.e("BoardGame", "Error in directCardTouch: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            // רישום המיקום למטרות דיבוג
            Log.d("BoardGame", "Touch at X: " + touchX + ", Y: " + touchY + ", scrollOffset: " + scrollOffset);

            // בדיקת כפתור "אין לי"
            if (isIDontHaveButtonTouched(touchX, touchY)) {
                Log.d("BoardGame", "I don't have button touched");
                handleIDontHave();
                return true;
            }

            // בדיקת כפתורי גלילה
            if (isScrollLeftButtonTouched(touchX, touchY)) {
                Log.d("BoardGame", "Scroll left button touched");
                scrollOffset = Math.max(0, scrollOffset - 280);
                invalidate();
                return true;
            } else if (isScrollRightButtonTouched(touchX, touchY)) {
                Log.d("BoardGame", "Scroll right button touched");
                if (canScrollRight) {
                    scrollOffset += 280;
                    invalidate();
                }
                return true;
            }

            // בדיקת מגע בקלפים ובקופה רק אם זה התור של השחקן
            if (isMyTurn) {
                Log.d("BoardGame", "It's my turn, checking touches");

                // בדיקת מגע בקופה
                if (isPacketTouched(touchX, touchY)) {
                    Log.d("BoardGame", "Packet touched");
                    handlePacketTouch();
                    return true;
                }

                // בדיקת מגע בקלפים - גישה חדשה לגמרי
                int cardY = getHeight() - 450; // המיקום הקבוע של Y לכל הקלפים
                int spacing = 280; // המרווח בין הקלפים
                int startX = (getWidth() - (myCards.size() * spacing)) / 2 - scrollOffset; // מיקום התחלתי מתוקן

                for (int i = 0; i < myCards.size(); i++) {
                    Card card = myCards.get(i);
                    int cardX = startX + (i * spacing); // חישוב מיקום X לכל קלף לפי האינדקס

                    // רישום המיקום של כל קלף למטרות דיבוג
                    Log.d("BoardGame", "Card " + i + " (" + card.getCardName() + ") at X: " + cardX);

                    // בדיקה האם המגע נמצא בתוך שטח הקלף
                    if (touchX >= cardX && touchX <= cardX + 260 &&
                            touchY >= cardY && touchY <= cardY + 400) {

                        Log.d("BoardGame", "Card touched: " + card.getCatagory() + " - " + card.getCardName());
                        directCardTouch(card);
                        return true;
                    }
                }
            } else {
                Log.d("BoardGame", "Not my turn, ignoring touches");
            }
        }
        return super.onTouchEvent(event);
    }

    private boolean isScrollLeftButtonTouched(float x, float y) { // פונקציה שבודקת אם המגע היה על כפתור גלילה שמאלה
        return canScrollLeft && x >= 20 && x <= 20 + SCROLL_BUTTON_WIDTH && // בדיקה אם ניתן לגלול שמאלה והמגע היה בתחום ה-X של הכפתור
                y >= getHeight() - 450 - SCROLL_BUTTON_HEIGHT / 2 && y <= getHeight() - 450 + SCROLL_BUTTON_HEIGHT / 2; // בדיקה אם המגע היה בתחום ה-Y של הכפתור
    }

    private boolean isScrollRightButtonTouched(float x, float y) { // פונקציה שבודקת אם המגע היה על כפתור גלילה ימינה
        return canScrollRight && x >= getWidth() - 20 - SCROLL_BUTTON_WIDTH && x <= getWidth() - 20 && // בדיקה אם ניתן לגלול ימינה והמגע היה בתחום ה-X של הכפתור
                y >= getHeight() - 450 - SCROLL_BUTTON_HEIGHT / 2 && y <= getHeight() - 450 + SCROLL_BUTTON_HEIGHT / 2; // בדיקה אם המגע היה בתחום ה-Y של הכפתור
    }

    private boolean isPacketTouched(float x, float y) { // פונקציה שבודקת אם המגע היה על הקופה
        int packetX = getWidth() / 2 - 130; // חישוב מיקום X של הקופה
        int packetY = getHeight() / 2 - 200; // חישוב מיקום Y של הקופה
        return x >= packetX && x <= packetX + 260 && // בדיקה אם המגע היה בתחום ה-X של הקופה
                y >= packetY && y <= packetY + 400; // בדיקה אם המגע היה בתחום ה-Y של הקופה
    }

    private void handlePacketTouch() { // פונקציה שמטפלת במגע על הקופה
        if (!packet.isEmpty()) { // בדיקה אם הקופה אינה ריקה
            Card drawnCard = packet.drawCard(); // משיכת קלף מהקופה
            if (drawnCard != null) { // בדיקה אם נמשך קלף
                myCards.add(drawnCard); // הוספת הקלף ליד של השחקן הנוכחי

                // עדכון Firebase עם הקלפים החדשים של השחקן
                if (isPlayer1) { // בדיקה אם זה שחקן 1
                    fbModule.updatePlayer1Cards(myCards); // עדכון קלפי שחקן 1 ב-Firebase
                } else { // אם זה שחקן 2
                    fbModule.updatePlayer2Cards(myCards); // עדכון קלפי שחקן 2 ב-Firebase
                }

                // עדכון Firebase עם הקופה החדשה לאחר משיכת קלף
                fbModule.updatePacket(packet.getAllCards()); // עדכון הקופה ב-Firebase

                // בדיקה של רביעייה (Rummy) לפני החלפת תור
                if (checkForQuartet(myCards)) { // בדיקה אם יש רביעייה ביד של השחקן
                    removeQuartetFromHand(myCards); // הסרת הרביעייה מהיד של השחקן
                } else { // אם אין רביעייה
                    // החלפת תור לאחר משיכת קלף
                    fbModule.switchTurn(); // החלפת התור ב-Firebase
                }

                // עדכון המסך
                invalidate(); // גורם לציור מחדש של התצוגה

                // בדיקת תנאי סיום משחק
                checkEndGame(); // בדיקה אם המשחק הסתיים
            }
        }
    }


    private void initializeGame() {
        try {
            deck = new Deck(); // יצירת מופע חדש של החפיסה
            deck.AddForDeck(ListOfCards); // הוספת כל הקלפים לחפיסה
            deck.Shuffle(ListOfCards); // ערבוב הקלפים בחפיסה

            // חלוקת קלפים ראשונית
            ArrayList<Card> initialCards = new ArrayList<>(); // יצירת מערך זמני לקלפים הראשוניים
            for (int i = 0; i < 4 && i < ListOfCards.length; i++) { // לולאה שרצה 4 פעמים (4 קלפים ראשוניים)
                if (isPlayer1) { // בדיקה אם זה שחקן 1
                    initialCards.add(ListOfCards[i]); // הוספת הקלף למערך הקלפים הראשוניים
                } else { // אם זה שחקן 2
                    if (i + 4 < ListOfCards.length) {
                        initialCards.add(ListOfCards[i + 4]); // הוספת הקלף למערך הקלפים הראשוניים (החל מהקלף החמישי)
                    }
                }
            }
            myCards = initialCards; // השמת הקלפים הראשוניים ליד של השחקן הנוכחי

            // הוספת הקלפים הנותרים לקופה
            ArrayList<Card> remainingCards = new ArrayList<>(); // יצירת מערך זמני לקלפים הנותרים
            for (int i = 8; i < ListOfCards.length; i++) { // לולאה שרצה על כל הקלפים שנותרו (החל מהקלף התשיעי)
                remainingCards.add(ListOfCards[i]); // הוספת הקלף למערך הקלפים הנותרים
            }
            packet.setCards(remainingCards); // השמת הקלפים הנותרים לקופה

            // עדכון Firebase
            if (fbModule != null) {
                if (isPlayer1) { // בדיקה אם זה שחקן 1
                    fbModule.updatePlayer1Cards(myCards); // עדכון קלפי שחקן 1 ב-Firebase
                } else { // אם זה שחקן 2
                    fbModule.updatePlayer2Cards(myCards); // עדכון קלפי שחקן 2 ב-Firebase
                }
                fbModule.updatePacket(remainingCards); // עדכון הקופה ב-Firebase
            }
        } catch (Exception e) {
            Log.e("BoardGame", "שגיאה באתחול המשחק: " + e.getMessage());
        }
        invalidate(); // גורם לציור מחדש של התצוגה
        if (fbModule != null) {
            fbModule.debugCardStates();

            // Ensure turn is set properly
            if (isPlayer1) {
                fbModule.gameStateRef.child("currentTurn").setValue("player1");
            }
        }
    }

    // הוספת פונקציה להגדרת צבע הרקע
    public void setBackgroundColor(String color) {
        // רק שינוי הצבע של הרקע, בלי לעדכן את Firebase כדי להימנע ממעגל אינסופי
        int colorValue;
        switch (color) {
            case "Blue": colorValue = Color.BLUE; break;
            case "Red": colorValue = Color.RED; break;
            case "Pink": colorValue = 0xFFF2ACB9; break;
            case "Yellow": colorValue = Color.YELLOW; break;
            default: colorValue = Color.parseColor("#D6EAF8");
        }

        backgroundPaint.setColor(colorValue);
        invalidate(); // ציור מחדש של המסך
    }

    @Override
    protected void onDraw(Canvas canvas) { // פונקציה שמציירת את התצוגה
        super.onDraw(canvas); // קריאה לפונקצית הציור של המחלקה הבסיסית

        // ציור רקע
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint); // ציור מלבן ברוחב וגובה המסך בצבע הרקע (עבד, אך לאחר הוספת הרקע פחות)

        // ציור רכיבי המשחק
        drawPacket(canvas); // ציור הקופה
        drawPlayerCards(canvas, myCards); // ציור הקלפים של השחקן הנוכחי
        drawOpponentInfo(canvas); // ציור מידע על היריב
        drawScores(canvas); // ציור הניקוד
        drawScrollButtons(canvas); // ציור כפתורי הגלילה

        // ציור כפתור "I don't have"
        createIDontHaveButton(canvas); // יצירת כפתור "אין לי"

        // הודעת תור
        if (isMyTurn) { // בדיקה אם זה התור של השחקן הנוכחי
            Paint textPaint = new Paint(); // יצירת אובייקט Paint לטקסט
            textPaint.setColor(Color.RED); // הגדרת צבע הטקסט לאדום
            textPaint.setTextSize(60); // הגדרת גודל הטקסט ל-60
            textPaint.setTextAlign(Paint.Align.CENTER); // הגדרת יישור הטקסט למרכז
            canvas.drawText("Your Turn To Be Asked!", getWidth() / 2, 200, textPaint); // ציור הטקסט במרכז המסך
        }

        // בדיקת סיום משחק
        if (packet.isEmpty() && (myCards.isEmpty() || opponentCards.isEmpty())) { // בדיקה אם הקופה ריקה ולפחות לאחד מהשחקנים אין קלפים
            drawGameOver(canvas); // ציור מסך סיום המשחק
        }
    }

    private void drawScrollButtons(Canvas canvas) {
        Paint buttonPaint = new Paint();
        buttonPaint.setColor(Color.GRAY);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // חישוב מתוקן
        int cardWidth = 280; // הרוחב של כל קלף
        int totalCardsWidth = myCards.size() * cardWidth; // רוחב כל הקלפים
        int visibleWidth = getWidth() - 40; // רוחב המסך הנראה בניכוי השוליים

        // מקסימום גלילה - מאפשר לראות את הקלף האחרון במלואו
        int maxScrollOffset = Math.max(0, totalCardsWidth - visibleWidth + 150); // שוליים גדולים יותר

        // עדכון ה-scrollOffset אם הוא גדול מדי
        if (scrollOffset > maxScrollOffset) {
            scrollOffset = maxScrollOffset;
        }

        canScrollLeft = scrollOffset > 0;
        canScrollRight = scrollOffset < maxScrollOffset;

        // ציור כפתורי הגלילה
        if (canScrollLeft) {
            canvas.drawRect(20, getHeight() - 450 - SCROLL_BUTTON_HEIGHT/2,
                    20 + SCROLL_BUTTON_WIDTH, getHeight() - 450 + SCROLL_BUTTON_HEIGHT/2, buttonPaint);
            canvas.drawText("<", 20 + SCROLL_BUTTON_WIDTH/2, getHeight() - 450 + 15, textPaint);
        }

        if (canScrollRight) {
            canvas.drawRect(getWidth() - 20 - SCROLL_BUTTON_WIDTH, getHeight() - 450 - SCROLL_BUTTON_HEIGHT/2,
                    getWidth() - 20, getHeight() - 450 + SCROLL_BUTTON_HEIGHT/2, buttonPaint);
            canvas.drawText(">", getWidth() - 20 - SCROLL_BUTTON_WIDTH/2, getHeight() - 450 + 15, textPaint);
        }
    }

    // יצירת כפתור "I don't have"
    private void createIDontHaveButton(Canvas canvas) { // פונקציה שיוצרת את כפתור "אין לי"
        // מיקום הכפתור מעל הקופה
        int packetX = getWidth() / 2 - 130; // חישוב מיקום X של הקופה
        int packetY = getHeight() / 2 - 200; // חישוב מיקום Y של הקופה

        // שמירת מיקום הכפתור
        iDontHaveButtonX = packetX; // השמת מיקום X של הכפתור
        iDontHaveButtonY = packetY - iDontHaveButtonHeight - 20; // השמת מיקום Y של הכפתור (מעל הקופה)

        // צביעת הכפתור
        Paint buttonPaint = new Paint(); // יצירת אובייקט Paint לכפתור
        buttonPaint.setColor(Color.LTGRAY); // הגדרת צבע הכפתור לאפור בהיר
        buttonPaint.setStyle(Paint.Style.FILL); // הגדרת סגנון הציור למילוי

        // ציור מלבן עבור הכפתור
        canvas.drawRect(
                iDontHaveButtonX, // מיקום X של הפינה השמאלית העליונה
                iDontHaveButtonY, // מיקום Y של הפינה השמאלית העליונה
                iDontHaveButtonX + iDontHaveButtonWidth, // מיקום X של הפינה הימנית התחתונה
                iDontHaveButtonY + iDontHaveButtonHeight, // מיקום Y של הפינה הימנית התחתונה
                buttonPaint // אובייקט הצביעה
        );
        // הוספת טקסט לכפתור
        Paint textPaint = new Paint(); // יצירת אובייקט Paint לטקסט
        textPaint.setColor(Color.BLACK); // הגדרת צבע הטקסט לשחור
        textPaint.setTextSize(40); // הגדרת גודל הטקסט ל-40
        textPaint.setTextAlign(Paint.Align.CENTER); // הגדרת יישור הטקסט למרכז

        canvas.drawText(
                "I don't have", // הטקסט שיוצג על הכפתור
                iDontHaveButtonX + iDontHaveButtonWidth / 2, // מיקום X של הטקסט (מרכז הכפתור)
                iDontHaveButtonY + iDontHaveButtonHeight / 2 + 15, // מיקום Y של הטקסט (מרכז הכפתור + התאמה לגודל הטקסט)
                textPaint // אובייקט הצביעה
        );
    }

    // בדיקה האם לחצו על כפתור "I don't have"
    private boolean isIDontHaveButtonTouched(float x, float y) { // פונקציה שבודקת אם המגע היה על כפתור "אין לי"
        return x >= iDontHaveButtonX && // בדיקה אם מיקום X של המגע גדול או שווה למיקום X של הכפתור
                x <= iDontHaveButtonX + iDontHaveButtonWidth && // בדיקה אם מיקום X של המגע קטן או שווה למיקום X של הכפתור + רוחב הכפתור
                y >= iDontHaveButtonY && // בדיקה אם מיקום Y של המגע גדול או שווה למיקום Y של הכפתור
                y <= iDontHaveButtonY + iDontHaveButtonHeight; // בדיקה אם מיקום Y של המגע קטן או שווה למיקום Y של הכפתור + גובה הכפתור
    }

    // טיפול בלחיצה על כפתור "I don't have"
    private void handleIDontHave() { // פונקציה שמטפלת בלחיצה על כפתור "אין לי"
        if (isMyTurn && !packet.isEmpty()) { // בדיקה אם זה התור של השחקן הנוכחי והקופה אינה ריקה
            // שליפת קלף מהקופה
            Card drawnCard = packet.drawCard(); // משיכת קלף מהקופה

            // הוספת הקלף ליריב
            if (isPlayer1) { // בדיקה אם זה שחקן 1
                fbModule.moveCardToPlayer("player2", drawnCard); // העברת הקלף לשחקן 2
            } else { // אם זה שחקן 2
                fbModule.moveCardToPlayer("player1", drawnCard); // העברת הקלף לשחקן 1
            }

            // עדכון הקופה ב-Firebase
            fbModule.updatePacket(packet.getAllCards()); // עדכון הקופה ב-Firebase

            // החלפת תור
            fbModule.switchTurn(); // החלפת התור ב-Firebase

            // רענון המסך
            invalidate(); // גורם לציור מחדש של התצוגה
        }
    }private void drawGameOver(Canvas canvas) { // פונקציה שמציירת את מסך סיום המשחק
        Paint overlay = new Paint(); // יצירת אובייקט Paint לשכבת הכיסוי
        overlay.setColor(Color.argb(180, 0, 0, 0)); // הגדרת צבע שכבת הכיסוי לשחור עם שקיפות
        canvas.drawRect(0, 0, getWidth(), getHeight(), overlay); // ציור מלבן בגודל המסך עם שכבת הכיסוי

        Paint textPaint = new Paint(); // יצירת אובייקט Paint לטקסט
        textPaint.setColor(Color.WHITE); // הגדרת צבע הטקסט ללבן
        textPaint.setTextSize(80); // הגדרת גודל הטקסט ל-80
        textPaint.setTextAlign(Paint.Align.CENTER); // הגדרת יישור הטקסט למרכז

        String winner; // משתנה שיכיל את הטקסט המציין את המנצח
        if (myScore > opponentScore) { // בדיקה אם הניקוד של השחקן הנוכחי גדול מהניקוד של היריב
            winner = isPlayer1 ? "Player 1 Wins!" : "Player 2 Wins!"; // אם זה שחקן 1, המנצח הוא שחקן 1, אחרת המנצח הוא שחקן 2
        } else if (opponentScore > myScore) { // בדיקה אם הניקוד של היריב גדול מהניקוד של השחקן הנוכחי
            winner = isPlayer1 ? "Player 2 Wins!" : "Player 1 Wins!"; // אם זה שחקן 1, המנצח הוא שחקן 2, אחרת המנצח הוא שחקן 1
        } else { // אם הניקוד שווה
            winner = "It's a Tie!"; // תיקו
        }

        canvas.drawText("Game Over", getWidth()/2, getHeight()/2 - 100, textPaint); // ציור הטקסט "Game Over" במרכז המסך
        canvas.drawText(winner, getWidth()/2, getHeight()/2, textPaint); // ציור הטקסט המציין את המנצח במרכז המסך
        canvas.drawText("Score: " + myScore + " - " + opponentScore, getWidth()/2, getHeight()/2 + 100, textPaint); // ציור הטקסט המציג את הניקוד במרכז המסך
    }

    private void drawScores(Canvas canvas) { // פונקציה שמציירת את הניקוד
        Paint textPaint = new Paint(); // יצירת אובייקט Paint לטקסט
        textPaint.setColor(Color.BLACK); // הגדרת צבע הטקסט לשחור
        textPaint.setTextSize(40); // הגדרת גודל הטקסט ל-40
        textPaint.setTextAlign(Paint.Align.LEFT); // הגדרת יישור הטקסט לשמאל

        String playerLabel = isPlayer1 ? "Player 1" : "Player 2"; // משתנה המכיל את הטקסט המציין את השחקן הנוכחי
        String opponentLabel = isPlayer1 ? "Player 2" : "Player 1"; // משתנה המכיל את הטקסט המציין את היריב

        canvas.drawText(playerLabel + " Score: " + myScore, 50, 150, textPaint); // ציור הטקסט המציג את הניקוד של השחקן הנוכחי
        canvas.drawText(opponentLabel + " Score: " + opponentScore, 50, 200, textPaint); // ציור הטקסט המציג את הניקוד של היריב
    }

    public void updateScore(int score) { // פונקציה שמעדכנת את הניקוד
        opponentScore = score; // עדכון הניקוד של היריב
        invalidate(); // גורם לציור מחדש של התצוגה
    }

    private void drawPacket(Canvas canvas) { // פונקציה שמציירת את הקופה
        // ציור הקופה באדום כמו הקלפים
        Paint packetPaint = new Paint(); // יצירת אובייקט Paint לקופה
        packetPaint.setColor(Color.RED); // הגדרת צבע הקופה לאדום
        int packetX = getWidth() / 2 - 130; // חישוב מיקום X של הקופה
        int packetY = getHeight() / 2 - 200; // חישוב מיקום Y של הקופה
        canvas.drawRect(packetX, packetY, // ציור מלבן עבור הקופה
                packetX + 260, packetY + 400, packetPaint);

        // הוספת טקסט "Packet" על הקלף ומספר הקלפים
        Paint textPaint = new Paint(); // יצירת אובייקט Paint לטקסט
        textPaint.setColor(Color.BLACK); // הגדרת צבע הטקסט לשחור
        textPaint.setTextSize(50); // הגדרת גודל הטקסט ל-50
        textPaint.setTextAlign(Paint.Align.CENTER); // הגדרת יישור הטקסט למרכז
        canvas.drawText("Packet", packetX + 130, packetY + 180, textPaint); // ציור הטקסט "Packet" על הקופה
        canvas.drawText("(" + packet.size() + ")", packetX + 130, packetY + 240, textPaint); // ציור מספר הקלפים בקופה
    }

    private void drawPlayerCards(Canvas canvas, ArrayList<Card> cards) {
        int spacing = 280; // מרווח בין הקלפים
        int startX = (getWidth() - (cards.size() * spacing)) / 2; // חישוב מיקום התחלתי

        // התאמה לגלילה
        startX -= scrollOffset;

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int cardX = startX + (i * spacing);
            card.setX(cardX); // עדכון המיקום של הקלף במערך
            card.setY(getHeight() - 450);

            // ציור רק של קלפים שנראים או חלקית נראים על המסך
            if (cardX + 260 >= 0 && cardX <= getWidth()) {
                card.draw(canvas);

                // רישום מיקום הקלף למטרות דיבוג
                Log.d("BoardGame", "Drawing card " + i + " at X: " + cardX);
            }
        }
    }

    private void drawOpponentInfo(Canvas canvas) { // פונקציה שמציירת מידע על היריב
        Paint textPaint = new Paint(); // יצירת אובייקט Paint לטקסט
        textPaint.setColor(Color.BLACK); // הגדרת צבע הטקסט לשחור
        textPaint.setTextSize(40); // הגדרת גודל הטקסט ל-40
        textPaint.setTextAlign(Paint.Align.LEFT); // הגדרת יישור הטקסט לשמאל
        String opponentName = isPlayer1 ? "Player 2" : "Player 1"; // משתנה המכיל את הטקסט המציין את היריב
        canvas.drawText(opponentName + "'s Cards: " + opponentCards.size(), // ציור הטקסט המציג את מספר הקלפים של היריב
                50, 100, textPaint);
    }

    public void updatePlayerCards(ArrayList<Card> cards, boolean isPlayer1Update) {
        Log.d("BoardGame", "updatePlayerCards: isPlayer1Update=" + isPlayer1Update +
                ", cards=" + (cards != null ? cards.size() : "null") +
                ", isPlayer1=" + isPlayer1);

        // עדכון המערך הגלובלי המתאים
        if (isPlayer1Update) {
            player1Cards = new ArrayList<>(cards);
        } else {
            player2Cards = new ArrayList<>(cards);
        }

        // עדכון הקלפים שלי ושל היריב בהתאם למי אני
        if (isPlayer1) {
            // אני שחקן 1
            if (isPlayer1Update) {
                myCards = new ArrayList<>(cards);
            } else {
                opponentCards = new ArrayList<>(cards);
            }
        } else {
            // אני שחקן 2
            if (isPlayer1Update) {
                opponentCards = new ArrayList<>(cards);
            } else {
                myCards = new ArrayList<>(cards);
            }
        }

        updateOpponentCardCount();
        Log.d("BoardGame", "After update: myCards=" + myCards.size() + ", opponentCards=" + opponentCards.size());
        invalidate();
    }
    private void updateOpponentCardCount() { // פונקציה שמעדכנת את מספר הקלפים של היריב
        if (tvOpponentCards != null) { // בדיקה אם יש TextView להצגת מספר הקלפים של היריב
            String opponentName = isPlayer1 ? "Player 2" : "Player 1"; // משתנה המכיל את הטקסט המציין את היריב
            tvOpponentCards.setText(opponentName + "'s Cards: " + opponentCards.size()); // עדכון הטקסט המציג את מספר הקלפים של היריב
        }
    }

    public void setTurn(boolean isMyTurn) { // פונקציה שמגדירה את התור
        this.isMyTurn = isMyTurn; // עדכון המשתנה המציין אם זה התור של השחקן הנוכחי
        invalidate(); // גורם לציור מחדש של התצוגה
    }

    public void updatePacket(ArrayList<Card> cards) { // פונקציה שמעדכנת את הקופה
        packet.setCards(cards); // עדכון הקלפים בקופה
        invalidate(); // גורם לציור מחדש של התצוגה
    }

    /**
     * בדיקה אם יש לשחקן רביעייה (4 קלפים מאותה קטגוריה)
     */
    public boolean checkForQuartet(ArrayList<Card> cards) { // פונקציה שבודקת אם יש רביעייה
        if (cards.size() < 4) { // בדיקה אם יש פחות מ-4 קלפים
            return false; // אין מספיק קלפים לרביעייה
        }

        // ספירת קלפים לפי קטגוריה
        java.util.HashMap<String, Integer> categoryCount = new java.util.HashMap<>(); // מפה שסופרת את מספר הקלפים בכל קטגוריה
        for (Card card : cards) { // לולאה שרצה על כל הקלפים
            String category = card.getCatagory(); // קבלת הקטגוריה של הקלף
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1); // הוספת 1 למספר הקלפים בקטגוריה
        }

        // בדיקה אם יש קטגוריה עם 4 קלפים או יותר
        for (String category : categoryCount.keySet()) { // לולאה שרצה על כל הקטגוריות
            if (categoryCount.get(category) >= 4) { // בדיקה אם יש 4 קלפים או יותר בקטגוריה
                return true; // יש רביעייה
            }
        }

        return false; // אין רביעייה
    }

    /**
     * הסרת רביעייה מהיד של השחקן ועדכון הניקוד
     */
    public void removeQuartetFromHand(ArrayList<Card> cards) { // פונקציה שמסירה רביעייה מהיד של השחקן
        if (!checkForQuartet(cards)) { // בדיקה אם אין רביעייה
            return; // אין מה להסיר
        }

        // מציאת הקטגוריה הראשונה עם 4 קלפים או יותר
        java.util.HashMap<String, Integer> categoryCount = new java.util.HashMap<>(); // מפה שסופרת את מספר הקלפים בכל קטגוריה
        for (Card card : cards) { // לולאה שרצה על כל הקלפים
            String category = card.getCatagory(); // קבלת הקטגוריה של הקלף
            categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1); // הוספת 1 למספר הקלפים בקטגוריה
        }

        String quartetCategory = null; // משתנה שיכיל את הקטגוריה של הרביעייה
        for (String category : categoryCount.keySet()) { // לולאה שרצה על כל הקטגוריות
            if (categoryCount.get(category) >= 4) { // בדיקה אם יש 4 קלפים או יותר בקטגוריה
                quartetCategory = category; // שמירת הקטגוריה של הרביעייה
                break; // יציאה מהלולאה
            }
        }

        if (quartetCategory != null) { // בדיקה אם נמצאה קטגוריה עם רביעייה
            // מציאת והסרת בדיוק 4 קלפים מקטגוריית הרביעייה
            ArrayList<Card> cardsToRemove = new ArrayList<>(); // מערך של קלפים להסרה
            int count = 0; // מונה של קלפים שנמצאו

            for (Card card : cards) { // לולאה שרצה על כל הקלפים
                if (card.getCatagory().equals(quartetCategory) && count < 4) { // בדיקה אם הקלף מהקטגוריה הנכונה ועדיין לא נמצאו 4 קלפים
                    cardsToRemove.add(card); // הוספת הקלף למערך הקלפים להסרה
                    count++; // הגדלת המונה ב-1
                }

                if (count == 4) { // בדיקה אם נמצאו 4 קלפים
                    break; // יציאה מהלולאה
                }
            }

            // הסרת הקלפים
            cards.removeAll(cardsToRemove); // הסרת כל הקלפים במערך הקלפים להסרה

            // עדכון ניקוד
            myScore += 40; // הוספת 40 נקודות לניקוד של השחקן הנוכחי

            // עדכון Firebase
            if (isPlayer1) { // בדיקה אם זה שחקן 1
                fbModule.updatePlayerScore("player1", myScore); // עדכון הניקוד של שחקן 1 ב-Firebase
                fbModule.updatePlayer1Cards(cards); // עדכון קלפי שחקן 1 ב-Firebase
            } else { // אם זה שחקן 2
                fbModule.updatePlayerScore("player2", myScore); // עדכון הניקוד של שחקן 2 ב-Firebase
                fbModule.updatePlayer2Cards(cards); // עדכון קלפי שחקן 2 ב-Firebase
            }

            // בדיקת סיום משחק או רביעייה נוספת
            if (cards.isEmpty() || cards.size() < 4) { // בדיקה אם אין קלפים או פחות מ-4 קלפים
                checkEndGame(); // בדיקה אם המשחק הסתיים
            } else if (checkForQuartet(cards)) { // בדיקה אם יש רביעייה נוספת
                // טיפול ברביעייה נוספת אם קיימת
                removeQuartetFromHand(cards); // הסרת הרביעייה הנוספת
            }

            invalidate(); // גורם לציור מחדש של התצוגה
        }
    }

    private void checkEndGame() { // פונקציה שבודקת אם המשחק הסתיים
        // המשחק מסתיים כאשר הקופה ריקה ולפחות לאחד מהשחקנים אין קלפים
        // או כאשר לשני השחקנים אין קלפים
        if ((packet.isEmpty() && (myCards.isEmpty() || opponentCards.isEmpty())) ||
                (myCards.isEmpty() && opponentCards.isEmpty())) { // בדיקת תנאי סיום המשחק

            // קביעת המנצח
            String winner; // משתנה שיכיל את שם המנצח
            int winnerScore; // משתנה שיכיל את הניקוד של המנצח
            if (myScore > opponentScore) { // בדיקה אם הניקוד של השחקן הנוכחי גדול מהניקוד של היריב
                winner = isPlayer1 ? "Player 1" : "Player 2"; // אם זה שחקן 1, המנצח הוא שחקן 1, אחרת המנצח הוא שחקן 2
                winnerScore = myScore; // הניקוד של המנצח הוא הניקוד של השחקן הנוכחי
            } else if (opponentScore > myScore) { // בדיקה אם הניקוד של היריב גדול מהניקוד של השחקן הנוכחי
                winner = isPlayer1 ? "Player 2" : "Player 1"; // אם זה שחקן 1, המנצח הוא שחקן 2, אחרת המנצח הוא שחקן 1
                winnerScore = opponentScore; // הניקוד של המנצח הוא הניקוד של היריב
            } else { // אם הניקוד שווה
                winner = "It's a Tie"; // תיקו
                winnerScore = myScore; // הניקוד של "המנצח" הוא הניקוד של השחקן הנוכחי (שניהם שווים)
            }

            // הצגת דיאלוג מותאם אישית עם המנצח
            if (getContext() != null) { // בדיקה אם יש קונטקסט
                CustomDialog customDialog = new CustomDialog(getContext()); // יצירת דיאלוג מותאם אישית
                customDialog.showWinnerDialog(winner, winnerScore); // הצגת הדיאלוג עם המנצח והניקוד
            }

            // עדכון ממשק המשתמש
            invalidate(); // גורם לציור מחדש של התצוגה
        }
    }
    private boolean areCardsEqual(Card card1, Card card2) {
        boolean result = card1.equals(card2);
        Log.d("BoardGame", "Card comparison: " + card1.getCatagory() + ":" + card1.getCardName() +
                " vs " + card2.getCatagory() + ":" + card2.getCardName() + " = " + result);
        return result;
    }
}