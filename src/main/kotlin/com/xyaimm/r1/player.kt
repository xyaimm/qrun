package com.xyaimm.r1

import java.util.*

open class Player(val playerEvent: PlayerEvent) {
    val pokers = mutableListOf<Poker>()
    var lastPlaySize = 0

    fun isVictory() = pokers.isEmpty()

    fun getCard(poker: Collection<Poker>) {
        pokers.addAll(poker)
    }

    fun play(poker: Collection<Poker>, strandFist: Int = 0): Pokers {
        pokers.removeAll(poker)
        val pokers = Pokers.create(poker, strandFist)
        playerEvent.play(this, pokers)
        lastPlaySize = pokers.size
        return pokers

    }

    fun jump() {
        playerEvent.jump(this)
    }

    fun canPlay(poker: List<Poker>, lastPokers: Pokers = Pokers.create(listOf())): Boolean {
        val thisPokers = Pokers.create(poker)
        return when (lastPokers) {
            is PokersSingle -> {
                when (thisPokers) {
                    is PokersSingle -> {
                        if (lastPokers.number == 2) {
                            false
                        } else {
                            thisPokers.number == 2 || thisPokers.number - lastPokers.number == 1
                        }
                    }
                    is PokersBomb -> true
                    else -> false
                }
            }
            is PokersPair -> when (thisPokers) {
                is PokersPair -> {
                    if (lastPokers.number == 2) {
                        false
                    } else {
                        thisPokers.number == 2 || thisPokers.number - lastPokers.number == 1
                    }
                }
                is PokersBomb -> true
                else -> false
            }
            is PokersBomb -> if (thisPokers is PokersBomb) {
                when {
                    thisPokers.size > lastPokers.size -> true
                    thisPokers.size == lastPokers.size -> thisPokers.number > lastPokers.number
                    else -> false
                }
            } else {
                false
            }
            is PokersStrand -> when (thisPokers) {
                is PokersStrand -> {
                    thisPokers.size == lastPokers.size && thisPokers.canFist.contains(lastPokers.fist + 1)
                }
                is PokersBomb -> true
                else -> false
            }
            is PokersNotOut -> thisPokers !is PokersNotOut
        }
    }


    override fun toString(): String {
        return pokers.toString()
    }

    fun waitPlay(lastPokers: Pokers) {
        val str = Scanner(System.`in`).next()
        if (!str.matches(Regex("^[0-9]{1,7}$"))) {
            jump()
            return
        }
        val li = str?.toCharArray()
                ?.map { "$it".toInt() }?.map {
                    pokers[it]
                }
                ?: listOf()
        if (canPlay(li, lastPokers)) {
            play(li)
        } else {
            println("不合法")
            waitPlay(lastPokers)
        }


    }


}

interface PlayerEvent {
    fun play(player: Player, poker: Pokers)
    fun jump(player: Player)
}

