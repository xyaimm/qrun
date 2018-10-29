package com.xyaimm.r1

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import java.util.*
import kotlin.math.min

class Desk(vertx: Vertx, private val pokers: Set<Poker> = Pokers.all) {
    private val remaining = LinkedList<Poker>()
    private val invalid = mutableSetOf<Poker>()
    private val players = mutableListOf<Player>()
    private var waitPlayer: Player? = null
    private var lastPlayer: Player? = null
    private var lastPokers: Pokers = Pokers.create(listOf())
    private val eb = vertx.eventBus()

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
        invalid.addAll(poker)
    }

    fun isEnd() = remaining.size < lastPokers.size

    fun fistGetPoker(player: Player = players.first()) {
        (1..6).forEach { _ ->
            players.forEach { it.getCard(getPokers(1)) }
        }
        player.getCard(getPokers(1))
        waitPlayer = player
        println("new:")
    }

    fun waitPlay() {
        players.forEach { pla ->
            if (pla == waitPlayer) {
                sendPlay(pla, "请输入")
            } else {
                senNotPlay(pla)
            }

        }
    }

    fun addPlayer(name: String): Player {
        val player = Player(name, object : PlayerEvent {
            override fun play(player: Player, poker: Pokers) {
                lastPlayer = player
                lastPokers = poker
                playCards(poker.pokers)
                if (player.isVictory()) {
                    println("游戏结束${player.name}胜利")
                    eb.publish("${hashCode()}", "游戏结束${player.name}胜利")
                } else {
                    waitPlayer = getNextPlayer(player)
                    waitPlay()
                }
            }

            override fun jump(player: Player) {
                waitPlayer = getNextPlayer(player)
                if (waitPlayer == lastPlayer) {
                    if (isEnd()) {
                        println("游戏结束牌用完")
                        eb.publish("${hashCode()}", "游戏结束牌用完")
                        return
                    }
                    waitPlayer?.getCard(getPokers(lastPokers.size))
                    lastPokers = Pokers.create(listOf())
                    println("new:")
                    eb.publish("${hashCode()}", "new:")
                }
                waitPlay()
            }

        })
        players.add(player)
        return player
    }

    private fun senNotPlay(player: Player) {
        eb.send("${hashCode()}${player.hashCode()}",
                JsonObject(mapOf<String, Any>(
                        "lastPokers" to lastPokers.pokers.map { it.toString() },
                        "pokers" to player.pokers.map { it.toString() },
                        "canPlay" to false,
                        "message" to "等待${waitPlayer?.name}输入"

                )))
    }

    private fun sendPlay(player: Player, msg: String) {
        eb.send<JsonObject>("${hashCode()}${player.hashCode()}",
                JsonObject(mapOf<String, Any>(
                        "lastPokers" to lastPokers.pokers.map { it.toString() },
                        "pokers" to player.pokers.map { it.toString() },
                        "canPlay" to true,
                        "message" to msg

                ))) { ar ->
            if (ar.succeeded()) {
                val message = ar.result()
                when (message.body().getString("event")) {
                    "play" -> {
                        val str = message.body().getString("pokers")
                        if (!(str ?: "").matches(Regex("^[0-9]{1,7}$"))) {
                            sendPlay(player, "输入不合法")
                            return@send
                        }
                        val li = str?.toCharArray()
                                ?.map { "$it".toInt() }?.map {
                                    player.pokers[it]
                                } ?: listOf()
                        if (!player.canPlay(li, lastPokers)) {
                            sendPlay(player, "输入不合法")
                            return@send
                        }
                        player.play(li, message.body().getString("strandFist").toInt())
                    }
                    "jump" -> {
                        player.jump()
                    }
                }
            }

        }
    }
}