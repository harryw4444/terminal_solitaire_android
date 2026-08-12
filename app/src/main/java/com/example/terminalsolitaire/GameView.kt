package com.example.terminalsolitaire

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val engine = SolitaireEngine()
    var isDarkMode = true
        set(value) {
            field = value
            updatePaints()
            invalidate()
        }

    private var selectedLocation: CardLocation? = null

    // Paints
    private val textPaint = Paint().apply {
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }
    private val redTextPaint = Paint().apply {
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
        color = Color.parseColor("#FF5252")
    }
    private val selectedTextPaint = Paint().apply {
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
        color = Color.YELLOW
    }
    private val cardBgPaint = Paint()

    private var charWidth = 0f
    private var charHeight = 0f
    private var colStep = 0f

    init {
        updatePaints()
    }

    private fun updatePaints() {
        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
        textPaint.color = textColor
        cardBgPaint.color = if (isDarkMode) Color.BLACK else Color.WHITE
        setBackgroundColor(if (isDarkMode) Color.BLACK else Color.WHITE)

        // Yellow highlight reads fine on a black background, but is very low
        // contrast on white, so light mode uses a dark blue instead.
        selectedTextPaint.color = if (isDarkMode) Color.YELLOW else Color.parseColor("#0D47A1")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            // Exactly 7 columns across full screen width
            colStep = w / 7f

            // Scale font size so 5 characters fit inside each column width comfortably
            val textSize = colStep / 5.2f
            textPaint.textSize = textSize
            redTextPaint.textSize = textSize
            selectedTextPaint.textSize = textSize

            charWidth = textPaint.measureText("M")
            val fontMetrics = textPaint.fontMetrics
            charHeight = fontMetrics.bottom - fontMetrics.top
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (charWidth == 0f) return

        // Center card within each column slot
        val cardOffset = (colStep - (5 * charWidth)) / 2f
        val topY = charHeight * 1.5f

        // --- ROW 1: Stock, Waste, & Foundations ---
        // Stock (Column 0)
        val stockX = cardOffset
        if (engine.stock.isEmpty()) {
            drawEmptySlot(canvas, stockX, topY)
        } else {
            drawCardBack(canvas, stockX, topY, isTopLeft = true)
        }

        // Waste (Column 1)
        val wasteX = colStep + cardOffset
        if (engine.waste.isNotEmpty()) {
            val topWaste = engine.waste.last()
            val wasteLoc = CardLocation(PileType.WASTE, 0)
            drawCardBox(canvas, topWaste, wasteX, topY, isSelected(wasteLoc))
        } else {
            drawEmptySlot(canvas, wasteX, topY)
        }

        // Foundations (Columns 3, 4, 5, 6)
        for (i in 0 until 4) {
            val fX = ((3 + i) * colStep) + cardOffset
            val foundation = engine.foundations[i]
            val fLoc = CardLocation(PileType.FOUNDATION, i)

            if (foundation.isNotEmpty()) {
                drawCardBox(canvas, foundation.last(), fX, topY, isSelected(fLoc))
            } else {
                drawEmptySlot(canvas, fX, topY)
            }
        }

        // --- ROW 2: Tableau Columns ---
        val tableauBaseY = topY + (charHeight * 5.5f)

        for (col in 0 until 7) {
            val colX = (col * colStep) + cardOffset
            val column = engine.tableaus[col]

            if (column.isEmpty()) {
                val tLoc = CardLocation(PileType.TABLEAU, col, 0)
                drawEmptySlot(canvas, colX, tableauBaseY, isSelected(tLoc))
            } else {
                var currentCardY = tableauBaseY
                for (row in column.indices) {
                    val card = column[row]
                    val loc = CardLocation(PileType.TABLEAU, col, row)

                    if (card.isFaceUp) {
                        drawCardBox(canvas, card, colX, currentCardY, isSelected(loc))
                        currentCardY += charHeight * 3.2f
                    } else {
                        drawCardBack(canvas, colX, currentCardY)
                        currentCardY += charHeight * 1.6f
                    }
                }
            }
        }
    }

    /**
     * Draws Face-Up Card using Unicode text suit symbols (\uFE0E forces monochrome text rendering)
     */
    private fun drawCardBox(canvas: Canvas, card: Card, x: Float, y: Float, isSelected: Boolean) {
        val paint = when {
            isSelected -> selectedTextPaint
            card.isRed -> redTextPaint
            else -> textPaint
        }

        // Solid background to block cards underneath
        canvas.drawRect(
            x - 1f,
            y - charHeight * 0.85f,
            x + (5 * charWidth) + 1f,
            y + (charHeight * 3.15f),
            cardBgPaint
        )

        val rankStr = card.rank.label.padStart(2, ' ')
        val suitStr = getUnicodeSuitSymbol(card.suit)

        // Row 0: Top Border
        drawTextAt(canvas, "┌", x, y, paint)
        drawTextAt(canvas, rankStr, x + charWidth, y, paint)
        drawTextAt(canvas, suitStr, x + (3 * charWidth), y, paint)
        drawTextAt(canvas, "┐", x + (4 * charWidth), y, paint)

        // Row 1 & 2: Sides
        val y1 = y + charHeight
        val y2 = y + (charHeight * 2f)
        drawTextAt(canvas, "│", x, y1, paint)
        drawTextAt(canvas, "│", x + (4 * charWidth), y1, paint)
        drawTextAt(canvas, "│", x, y2, paint)
        drawTextAt(canvas, "│", x + (4 * charWidth), y2, paint)

        // Row 3: Bottom Border
        val y3 = y + (charHeight * 3f)
        drawTextAt(canvas, "└", x, y3, paint)
        drawTextAt(canvas, rankStr, x + charWidth, y3, paint)
        drawTextAt(canvas, suitStr, x + (3 * charWidth), y3, paint)
        drawTextAt(canvas, "┘", x + (4 * charWidth), y3, paint)
    }

    private fun drawCardBack(canvas: Canvas, x: Float, y: Float, isTopLeft: Boolean = false) {
        canvas.drawRect(
            x - 1f,
            y - charHeight * 0.85f,
            x + (5 * charWidth) + 1f,
            y + (charHeight * 3.15f),
            cardBgPaint
        )

        val y1 = y + charHeight
        val y2 = y + (charHeight * 2f)
        val y3 = y + (charHeight * 3f)

        // Only the top-left card (the stock pile) shows a back pattern; all other
        // face-down card backs are left blank inside the border.
        val fill = if (isTopLeft) "///" else "   "

        drawTextAt(canvas, "┌", x, y, textPaint)
        drawTextAt(canvas, "───", x + charWidth, y, textPaint)
        drawTextAt(canvas, "┐", x + (4 * charWidth), y, textPaint)

        drawTextAt(canvas, "│", x, y1, textPaint)
        drawTextAt(canvas, fill, x + charWidth, y1, textPaint)
        drawTextAt(canvas, "│", x + (4 * charWidth), y1, textPaint)

        drawTextAt(canvas, "│", x, y2, textPaint)
        drawTextAt(canvas, fill, x + charWidth, y2, textPaint)
        drawTextAt(canvas, "│", x + (4 * charWidth), y2, textPaint)

        drawTextAt(canvas, "└", x, y3, textPaint)
        drawTextAt(canvas, "───", x + charWidth, y3, textPaint)
        drawTextAt(canvas, "┘", x + (4 * charWidth), y3, textPaint)
    }

    private fun drawEmptySlot(canvas: Canvas, x: Float, y: Float, isSelected: Boolean = false) {
        val paint = if (isSelected) selectedTextPaint else textPaint

        // Same height as a regular card (drawCardBox/drawCardBack) rather than a
        // smaller square, so empty foundation/tableau slots line up with cards.
        val y1 = y + charHeight
        val y2 = y + (charHeight * 2f)
        val y3 = y + (charHeight * 3f)

        drawTextAt(canvas, "┌", x, y, paint)
        drawTextAt(canvas, "───", x + charWidth, y, paint)
        drawTextAt(canvas, "┐", x + (4 * charWidth), y, paint)

        drawTextAt(canvas, "│", x, y1, paint)
        drawTextAt(canvas, "│", x + (4 * charWidth), y1, paint)

        drawTextAt(canvas, "│", x, y2, paint)
        drawTextAt(canvas, "│", x + (4 * charWidth), y2, paint)

        drawTextAt(canvas, "└", x, y3, paint)
        drawTextAt(canvas, "───", x + charWidth, y3, paint)
        drawTextAt(canvas, "┘", x + (4 * charWidth), y3, paint)
    }

    /**
     * Appends \uFE0E (Text Presentation Selector) to force Android to render monochrome text suit symbols
     */
    private fun getUnicodeSuitSymbol(suit: Suit): String {
        return when (suit) {
            Suit.CLUBS -> "♣\uFE0E"
            Suit.SPADES -> "♠\uFE0E"
            Suit.HEARTS -> "♥\uFE0E"
            Suit.DIAMONDS -> "♦\uFE0E"
        }
    }

    private fun drawTextAt(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        canvas.drawText(text, x, y, paint)
    }

    private fun isSelected(location: CardLocation): Boolean {
        val sel = selectedLocation ?: return false
        return sel.type == location.type &&
                sel.index == location.index &&
                sel.cardIndex == location.cardIndex
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            val cardOffset = (colStep - (5 * charWidth)) / 2f
            val topY = charHeight * 1.5f
            val cardW = 5 * charWidth

            // Stock tap
            val stockX = cardOffset
            if (x >= stockX && x <= stockX + cardW && y <= topY + (charHeight * 4f)) {
                engine.cycleStock()
                selectedLocation = null
                invalidate()
                return true
            }

            // Waste tap
            val wasteX = colStep + cardOffset
            if (x >= wasteX && x <= wasteX + cardW && y <= topY + (charHeight * 4f)) {
                if (engine.waste.isNotEmpty()) {
                    handleTap(CardLocation(PileType.WASTE, 0))
                }
                return true
            }

            // Foundation tap
            for (i in 0 until 4) {
                val fX = ((3 + i) * colStep) + cardOffset
                if (x >= fX && x <= fX + cardW && y <= topY + (charHeight * 4f)) {
                    handleTap(CardLocation(PileType.FOUNDATION, i))
                    return true
                }
            }

            // Tableau tap
            val tableauBaseY = topY + (charHeight * 5.5f)
            if (y >= tableauBaseY - charHeight) {
                val colIndex = (x / colStep).toInt()
                if (colIndex in 0..6) {
                    val column = engine.tableaus[colIndex]
                    if (column.isEmpty()) {
                        handleTap(CardLocation(PileType.TABLEAU, colIndex, 0))
                    } else {
                        var currentY = tableauBaseY
                        var targetRow = 0
                        for (row in column.indices) {
                            val nextY = currentY + if (column[row].isFaceUp) charHeight * 3.2f else charHeight * 1.6f
                            if (y >= currentY && (y < nextY || row == column.lastIndex)) {
                                targetRow = row
                                break
                            }
                            currentY = nextY
                        }
                        handleTap(CardLocation(PileType.TABLEAU, colIndex, targetRow))
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleTap(clickedLocation: CardLocation) {
        val sel = selectedLocation
        if (sel == null) {
            val handledAuto = engine.autoMove(clickedLocation)
            if (!handledAuto) {
                selectedLocation = clickedLocation
            }
        } else {
            val moved = engine.moveCards(sel, clickedLocation)
            selectedLocation = if (moved) null else clickedLocation
        }
        invalidate()
    }
}
