package com.example.terminalsolitaire

import kotlin.random.Random

enum class Suit(val symbol: String, val isRed: Boolean) {
    HEARTS("♥", true),
    DIAMONDS("♦", true),
    CLUBS("♣", false),
    SPADES("♠", false)
}

enum class Rank(val value: Int, val label: String) {
    ACE(1, "A"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"),
    FIVE(5, "5"), SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"),
    NINE(9, "9"), TEN(10, "10"), JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K")
}

data class Card(
    val suit: Suit,
    val rank: Rank,
    var isFaceUp: Boolean = false
) {
    val isRed: Boolean get() = suit.isRed
}

enum class PileType { STOCK, WASTE, FOUNDATION, TABLEAU }

data class CardLocation(
    val type: PileType,
    val index: Int = 0,
    val cardIndex: Int = 0
)

class SolitaireEngine {
    val stock = mutableListOf<Card>()
    val waste = mutableListOf<Card>()
    val foundations = List(4) { mutableListOf<Card>() }
    val tableaus = List(7) { mutableListOf<Card>() }

    init {
        startNewGame()
    }

    fun startNewGame() {
        stock.clear()
        waste.clear()
        foundations.forEach { it.clear() }
        tableaus.forEach { it.clear() }

        val deck = mutableListOf<Card>()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                deck.add(Card(suit, rank))
            }
        }
        deck.shuffle(Random(System.currentTimeMillis()))

        for (i in 0 until 7) {
            for (j in i until 7) {
                val card = deck.removeAt(0)
                if (i == j) {
                    card.isFaceUp = true
                }
                tableaus[j].add(card)
            }
        }

        stock.addAll(deck)
    }

    fun cycleStock() {
        if (stock.isNotEmpty()) {
            val card = stock.removeAt(stock.size - 1)
            card.isFaceUp = true
            waste.add(card)
        } else if (waste.isNotEmpty()) {
            while (waste.isNotEmpty()) {
                val card = waste.removeAt(waste.size - 1)
                card.isFaceUp = false
                stock.add(card)
            }
        }
    }

    fun moveCards(from: CardLocation, to: CardLocation): Boolean {
        return when (from.type) {
            PileType.WASTE -> when (to.type) {
                PileType.FOUNDATION -> moveWasteToFoundation(to.index)
                PileType.TABLEAU -> moveWasteToTableau(to.index)
                else -> false
            }

            PileType.TABLEAU -> when (to.type) {
                PileType.FOUNDATION -> moveTableauToFoundation(from.index, to.index)
                PileType.TABLEAU -> moveTableauToTableau(from.index, from.cardIndex, to.index)
                else -> false
            }

            else -> false
        }
    }

    fun autoMove(from: CardLocation): Boolean {
        for (i in foundations.indices) {
            if (moveCards(from, CardLocation(PileType.FOUNDATION, i))) {
                return true
            }
        }
        for (i in tableaus.indices) {
            if (moveCards(from, CardLocation(PileType.TABLEAU, i))) {
                return true
            }
        }
        return false
    }

    private fun moveWasteToFoundation(foundationIndex: Int): Boolean {
        if (waste.isEmpty()) return false
        val card = waste.last()
        val foundation = foundations[foundationIndex]

        if (canMoveToFoundation(card, foundation)) {
            foundation.add(waste.removeAt(waste.size - 1))
            return true
        }
        return false
    }

    private fun moveWasteToTableau(tableauIndex: Int): Boolean {
        if (waste.isEmpty()) return false
        val card = waste.last()
        val column = tableaus[tableauIndex]

        if (canMoveToTableau(card, column)) {
            column.add(waste.removeAt(waste.size - 1))
            return true
        }
        return false
    }

    private fun moveTableauToFoundation(tableauIndex: Int, foundationIndex: Int): Boolean {
        val column = tableaus[tableauIndex]
        if (column.isEmpty()) return false

        val card = column.last()
        val foundation = foundations[foundationIndex]

        if (canMoveToFoundation(card, foundation)) {
            foundation.add(column.removeAt(column.size - 1))
            ensureTopCardFaceUp(column)
            return true
        }
        return false
    }

    private fun moveTableauToTableau(fromCol: Int, cardIndex: Int, toCol: Int): Boolean {
        val sourceCol = tableaus[fromCol]
        val destCol = tableaus[toCol]

        if (cardIndex < 0 || cardIndex >= sourceCol.size) return false
        val movingCard = sourceCol[cardIndex]

        if (!movingCard.isFaceUp) return false

        if (canMoveToTableau(movingCard, destCol)) {
            val movingCards = sourceCol.subList(cardIndex, sourceCol.size).toList()
            repeat(movingCards.size) {
                sourceCol.removeAt(sourceCol.size - 1)
            }
            destCol.addAll(movingCards)
            ensureTopCardFaceUp(sourceCol)
            return true
        }
        return false
    }

    private fun canMoveToFoundation(card: Card, foundation: List<Card>): Boolean {
        return if (foundation.isEmpty()) {
            card.rank == Rank.ACE
        } else {
            val topCard = foundation.last()
            card.suit == topCard.suit && card.rank.value == topCard.rank.value + 1
        }
    }

    private fun canMoveToTableau(card: Card, column: List<Card>): Boolean {
        return if (column.isEmpty()) {
            card.rank == Rank.KING
        } else {
            val topCard = column.last()
            topCard.isFaceUp && card.isRed != topCard.isRed && card.rank.value == topCard.rank.value - 1
        }
    }

    private fun ensureTopCardFaceUp(column: MutableList<Card>) {
        if (column.isNotEmpty() && !column.last().isFaceUp) {
            column.last().isFaceUp = true
        }
    }

    fun isGameWon(): Boolean {
        return foundations.all { it.size == 13 }
    }

    /**
     * Serializes the full game state to a compact string so it can be restored
     * later (e.g. after the app is backgrounded and the process is killed).
     */
    fun serialize(): String {
        fun pileStr(pile: List<Card>) = pile.joinToString(",") { c ->
            "${c.suit.name[0]}${c.rank.value}${if (c.isFaceUp) "U" else "D"}"
        }

        val parts = mutableListOf(pileStr(stock), pileStr(waste))
        foundations.forEach { parts.add(pileStr(it)) }
        tableaus.forEach { parts.add(pileStr(it)) }
        return parts.joinToString(";")
    }

    /**
     * Restores game state previously produced by [serialize]. Returns true on
     * success; on any parse failure the engine is left untouched and false is
     * returned so the caller can fall back to a fresh game.
     */
    fun deserialize(data: String): Boolean {
        try {
            val parts = data.split(";")
            if (parts.size != 13) return false

            fun parsePile(s: String): MutableList<Card> {
                if (s.isEmpty()) return mutableListOf()
                return s.split(",").map { token ->
                    val suitChar = token[0]
                    val faceChar = token.last()
                    val rankValue = token.substring(1, token.length - 1).toInt()
                    val suit = Suit.values().first { it.name[0] == suitChar }
                    val rank = Rank.values().first { it.value == rankValue }
                    Card(suit, rank, faceChar == 'U')
                }.toMutableList()
            }

            val newStock = parsePile(parts[0])
            val newWaste = parsePile(parts[1])
            val newFoundations = List(4) { parsePile(parts[2 + it]) }
            val newTableaus = List(7) { parsePile(parts[6 + it]) }

            stock.clear(); stock.addAll(newStock)
            waste.clear(); waste.addAll(newWaste)
            for (i in 0 until 4) {
                foundations[i].clear(); foundations[i].addAll(newFoundations[i])
            }
            for (i in 0 until 7) {
                tableaus[i].clear(); tableaus[i].addAll(newTableaus[i])
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }
}
