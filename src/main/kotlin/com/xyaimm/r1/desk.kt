package com.xyaimm.r1

import java.util.*
import kotlin.math.min

class Desk(val pokers: Set<Poker> = Pokers.all, private val playerNum: Int = 4) {
    private val remaining = LinkedList<Poker>()
    private val invalid = mutableSetOf<Poker>()
    val players: List<Player> = (1..playerNum).map {
        Player(object : PlayerEvent {
            override fun play(player: Player, poker: Pokers) {
                lastPlayer = player
                lastPokers = poker
                playCards(poker.pokers)
                if (player.isVictory()) {
                    println("$player is Victory")
                } else {
                    waitPlayer = getNextPlayer(player)
                    waitPlay()
                }
            }

            override fun jump(player: Player) {
                waitPlayer = getNextPlayer(player)
                if (waitPlayer == lastPlayer) {
                    lastPokers = Pokers.create(listOf())
                }
                println("new :")
                waitPlay()
            }

        })
    }
    private var waitPlayer: Player = players.first()
    private var lastPlayer: Player = players.first()
    private var lastPokers: Pokers = Pokers.create(listOf())

    fun shuffle() {
        remaining.clear()
        invalid.clear()
        remaining.addAll(Pokers.shuffle(pokers))
    }

    fun getNextPlayer(player: Player): Player {
        val i = players.indexOf(player)
        return players[if (i == players.size - 1) {
            0
        } else {
            i + 1
        }]

    }

    fun getPokers(num: Int): List<Poker> =
            (1..(min(num, remaining.size))).map {
                remaining.removeAt(remaining.size - 1)
            }.toList()

    fun playCards(poker: Collection<Poker>) {
        invalid.addAll(pokers)
    }

    fun isEnd() = remaining.isEmpty()

    fun fistGetPoker(player: Player = players.first()) {
        (1..6).forEach { _ ->
            players.forEach { it.getCard(getPokers(1)) }
        }
        player.getCard(getPokers(1))
        waitPlayer = player
        println("new :")
    }

    fun waitPlay() {
        waitPlayer.waitPlay()
    }
}