package com.xyaimm.r1

import java.util.*
import kotlin.math.max
import kotlin.math.min

data class Poker(val color: PokerColor, val number: Int) {
    val colorName by lazy {
        when (color) {
            PokerColor.RedPeach -> "♥"
            PokerColor.Spades -> "♠"
            PokerColor.Square -> "♦"
            PokerColor.PlumBlossom -> "♣"
            else -> ""
        }
    }
    val numName by lazy {
        if (color == PokerColor.Joker) {
            when (number) {
                1 -> "R"
                else -> "B"
            }
        } else {
            when (number) {
                14 -> "A"
                11 -> "J"
                12 -> "Q"
                13 -> "K"
                else -> "$number"
            }
        }
    }

    override fun toString(): String {
        return colorName + numName
    }
}


enum class PokerColor {
    RedPeach, Spades, Square, PlumBlossom, Joker
}

sealed class Pokers(val pokers: Collection<Poker>) {
    companion object {
        val joker = setOf(Poker(PokerColor.Joker, 0), Poker(PokerColor.Joker, 1))
        val redPeach = setOf(Poker(PokerColor.RedPeach, 14), Poker(PokerColor.RedPeach, 2), Poker(PokerColor.RedPeach, 3), Poker(PokerColor.RedPeach, 4), Poker(PokerColor.RedPeach, 5), Poker(PokerColor.RedPeach, 6), Poker(PokerColor.RedPeach, 7), Poker(PokerColor.RedPeach, 8), Poker(PokerColor.RedPeach, 9), Poker(PokerColor.RedPeach, 10), Poker(PokerColor.RedPeach, 11), Poker(PokerColor.RedPeach, 12), Poker(PokerColor.RedPeach, 13))
        val spades = setOf(Poker(PokerColor.Spades, 14), Poker(PokerColor.Spades, 2), Poker(PokerColor.Spades, 3), Poker(PokerColor.Spades, 4), Poker(PokerColor.Spades, 5), Poker(PokerColor.Spades, 6), Poker(PokerColor.Spades, 7), Poker(PokerColor.Spades, 8), Poker(PokerColor.Spades, 9), Poker(PokerColor.Spades, 10), Poker(PokerColor.Spades, 11), Poker(PokerColor.Spades, 12), Poker(PokerColor.Spades, 13))
        val square = setOf(Poker(PokerColor.Square, 14), Poker(PokerColor.Square, 2), Poker(PokerColor.Square, 3), Poker(PokerColor.Square, 4), Poker(PokerColor.Square, 5), Poker(PokerColor.Square, 6), Poker(PokerColor.Square, 7), Poker(PokerColor.Square, 8), Poker(PokerColor.Square, 9), Poker(PokerColor.Square, 10), Poker(PokerColor.Square, 11), Poker(PokerColor.Square, 12), Poker(PokerColor.Square, 13))
        val plumBlossom = setOf(Poker(PokerColor.PlumBlossom, 14), Poker(PokerColor.PlumBlossom, 2), Poker(PokerColor.PlumBlossom, 3), Poker(PokerColor.PlumBlossom, 4), Poker(PokerColor.PlumBlossom, 5), Poker(PokerColor.PlumBlossom, 6), Poker(PokerColor.PlumBlossom, 7), Poker(PokerColor.PlumBlossom, 8), Poker(PokerColor.PlumBlossom, 9), Poker(PokerColor.PlumBlossom, 10), Poker(PokerColor.PlumBlossom, 11), Poker(PokerColor.PlumBlossom, 12), Poker(PokerColor.PlumBlossom, 13))
        val all = mutableSetOf<Poker>().apply { addAll(joker); addAll(redPeach); addAll(spades);addAll(square);addAll(plumBlossom) }.toSet()

        fun shuffle(pokers: Collection<Poker>): List<Poker> {
            val mAll = pokers.toMutableList()
            val random = Random()
            val size = mAll.size
            for (i in 0 until size) {
                val rInt = random.nextInt(size)
                val poker = mAll[i]
                mAll[i] = mAll[rInt]
                mAll[rInt] = poker
            }
            return mAll
        }

        fun create(pokers: Collection<Poker>, strandFist: Int = 0): Pokers {
            val p = pokers.toList()
            return when (p.size) {
                1 -> if (p.first().color == PokerColor.Joker) {
                    PokersNotOut(pokers)
                } else {
                    PokersSingle(pokers)
                }
                2 -> if (p[0].color == PokerColor.Joker) {
                    if (p[1].color == PokerColor.Joker) {
                        PokersNotOut(pokers)
                    } else {
                        PokersPair(pokers)
                    }
                } else {
                    if (p[0].number == p[1].number) {
                        PokersPair(pokers)
                    } else {
                        PokersNotOut(pokers)
                    }
                }
                3, 4, 5, 6, 7 -> {
                    val rj = removeJoker(p)
                    if (hasSameNum(rj)) {
                        if (rj.first().number == rj.last().number) {
                            PokersBomb(pokers)
                        } else {
                            PokersNotOut(pokers)
                        }
                    } else {
                        if (rj.first().number == 2) {
                            PokersNotOut(pokers)
                        } else {
                            val num = rj.last().number - rj.first().number + 1
                            if (num <= p.size && num >= rj.size) {
                                PokersStrand(pokers, strandFist)
                            } else {
                                PokersNotOut(pokers)
                            }
                        }
                    }
                }
                else -> PokersNotOut(pokers)
            }
        }

        fun removeJoker(poker: Collection<Poker>): List<Poker> =
                mutableListOf<Poker>().apply {
                    addAll(poker)
                    removeAll(Pokers.joker)
                    sortWith(Comparator { o1, o2 -> o1.number - o2.number })
                }

        fun hasSameNum(poker: Collection<Poker>) = poker.size != poker.map { it.number }.toSet().size
    }

    val size = pokers.size
}

class PokersSingle(pokers: Collection<Poker>) : Pokers(pokers) {
    val number: Int = pokers.toList().first().number
}

class PokersPair(pokers: Collection<Poker>) : Pokers(pokers) {
    val number: Int = removeJoker(pokers).first().number
}

class PokersBomb(pokers: Collection<Poker>) : Pokers(pokers) {
    val number: Int = removeJoker(pokers).first().number

}

class PokersStrand(pokers: Collection<Poker>, fistNumber: Int = 0) : Pokers(pokers) {
    val canFist: Set<Int>
    val fist: Int

    init {
        val rj = removeJoker(pokers)
        val num = rj.last().number - rj.first().number + 1
        canFist = when (num) {
            rj.size -> setOf(max(3, rj.first().number - 2), max(3, rj.first().number - 1), max(3, rj.first().number))
            rj.size + 1 -> setOf(max(3, rj.first().number - 2), max(3, rj.first().number - 1))
            else -> setOf(rj.first().number)
        }
        if (canFist.contains(fistNumber)) {
            fist = fistNumber
        } else {
            var n = 14
            canFist.forEach {
                n = min(n, it)
            }
            fist = n
        }
    }
}

class PokersNotOut(pokers: Collection<Poker>) : Pokers(pokers)