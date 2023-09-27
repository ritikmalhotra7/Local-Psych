package com.example.localpsych

import java.net.Socket

object SocketHandler {
    @get:Synchronized
    @set:Synchronized
    var socket: Socket? = null
}
