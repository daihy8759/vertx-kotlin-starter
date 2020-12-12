package com.github.daihy8759.service

import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.Command
import io.vertx.redis.client.Redis
import io.vertx.redis.client.Request

class TokenService(val redisClient: Redis) {

    suspend fun getToken(token: String): String? {
        return redisClient.send(Request.cmd(Command.GET).arg(token)).await().toString()
    }
}