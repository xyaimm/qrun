package com.xyaimm.r1


data class HomeReq(val event: String, val num: Int?)
data class HomeRes(val homeName: Int, val playerID: Int)
data class PokerMessage(val lastPokers: List<String>, val pokers: List<String>, val canPlay: Boolean)
data class PlayMessage(val event: String, val pokers: String)