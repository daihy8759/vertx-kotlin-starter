package com.github.daihy8759.service

import io.vertx.kotlin.redis.getAwait
import io.vertx.redis.RedisClient

class TokenService(val redisClient: RedisClient) {

    suspend fun getToken(token: String): String? {
        return redisClient.getAwait(token)
    }
}