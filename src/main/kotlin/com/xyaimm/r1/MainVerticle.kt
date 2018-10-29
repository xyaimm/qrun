package com.xyaimm.r1

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.kotlin.ext.web.handler.sockjs.BridgeOptions
import io.vertx.kotlin.ext.web.handler.sockjs.PermittedOptions

class MainVerticle : AbstractVerticle() {

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>) {

        var router = Router.router(vertx)
        var sockJSHandler = SockJSHandler.create(vertx)
        var inboundPermitted1 = PermittedOptions(
                addressRegex = ".*")
        var options = BridgeOptions(
                inboundPermitteds = listOf(inboundPermitted1),
                outboundPermitteds = listOf(inboundPermitted1))
        sockJSHandler.bridge(options)

        router.route("/eventbus/*").handler(sockJSHandler)

        router.route("/*").handler(StaticHandler.create())
        vertx.createHttpServer().requestHandler {
            router.accept(it)
        }.listen(8080) { http ->
            if (http.succeeded()) {
                startFuture.complete()
                println("HTTP server started on http://localhost:8080")
            } else {
                startFuture.fail(http.cause())
            }
        }
        var eb = vertx.eventBus()

        eb.consumer<JsonObject>("home") { message ->
            val desk: Desk
            when (message.body().getString("event")) {
                "create" -> {
                    desk = Desk(vertx)
                    homeMap[desk.hashCode()] = desk
                    val palyer = desk.addPlayer(message.body().getString("name"))
                    message.reply(JsonObject(mapOf("homeName" to "${desk.hashCode()}",
                            "playerID" to "${palyer.hashCode()}")))
                    eb.publish("${desk.hashCode()}", "${palyer.name}创建房间成功")
                }
                "join" -> {
                    desk = homeMap[message.body().getString("num").toInt()] ?: Desk(vertx)
                    val palyer = desk.addPlayer(message.body().getString("name"))
                    message.reply(JsonObject(mapOf("homeName" to "${desk.hashCode()}",
                            "playerID" to "${palyer.hashCode()}")))
                    eb.publish("${desk.hashCode()}", "${palyer.name}加入房间成功")
                }
                else -> {
                    desk = homeMap[message.body().getString("num").toInt()] ?: Desk(vertx)
                    desk.shuffle()
                    desk.fistGetPoker()
                    desk.waitPlay()
                    eb.publish("${desk.hashCode()}", "开始游戏")
                }
            }


        }
    }

    val homeMap = mutableMapOf<Int, Desk>()


}
