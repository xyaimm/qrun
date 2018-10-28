package com.xyaimm.r1

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
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
                address = "a")
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

        var consumer = eb.consumer<HomeReq>("home")
        consumer.handler { message ->
            val desk: Desk
            when (message.body().event) {
                "create" -> {
                    desk = Desk()
                    homeMap[desk.hashCode()] = desk
                    val palyer = desk.addPlayer(vertx)
                    message.reply(HomeRes(desk.hashCode(),palyer.hashCode()))
                }
                "join" -> {
                    desk = homeMap[message.body().num] ?: Desk()
                    val palyer = desk.addPlayer(vertx)
                    message.reply(HomeRes(desk.hashCode(),palyer.hashCode()))
                }
                else -> {
                    desk = homeMap[message.body().num] ?: Desk()
                    desk.shuffle()
                    desk.fistGetPoker()
                    desk.waitPlay()
                }
            }


        }
    }

    val homeMap = mutableMapOf<Int, Desk>()


}
