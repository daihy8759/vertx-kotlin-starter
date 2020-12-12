package com.github.daihy8759.verticle

import com.github.daihy8759.common.response.ApiResponse
import com.github.daihy8759.common.response.ConstantCode.TOKEN_REQUIRE_FAIL
import com.github.daihy8759.common.util.Constants
import com.github.daihy8759.common.util.VerticleClass
import com.github.daihy8759.keys.TokenKey
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.Command
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisOptions
import io.vertx.redis.client.Request
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.util.*

@VerticleClass
class TokenVerticle(val client: SqlClient) : CoroutineBaseVerticle() {

    private lateinit var redisClient: Redis
    private lateinit var constantConfig: JsonObject

    override suspend fun start() {
        constantConfig = config.getJsonObject("constant");
        redisClient = Redis.createClient(vertx, RedisOptions(config.getJsonObject("redis")))

        vertx.eventBus().coroutineConsumer(TokenKey.GET_TOKEN, this::getToken);
    }

    private suspend fun getToken(message: Message<JsonObject>) {
        val paramObject = message.body().getJsonObject(Constants.REQUEST_PARAM)
        val appId: String? = paramObject.getString("appId")
        val appSecret: String? = paramObject.getString("appSecret")
        if (appId.isNullOrBlank() || appSecret.isNullOrBlank()) {
            message.reply(ApiResponse(false, TOKEN_REQUIRE_FAIL))
            return;
        }
        if (exists(appId, appSecret)) {
            message.reply(createToken(appId))
        } else {
            message.reply(ApiResponse(false, TOKEN_REQUIRE_FAIL))
        }
    }

    private suspend fun createToken(appId: String): ApiResponse {
        val expiresIn = constantConfig.getLong("tokenExpire", 1200L)
        val token = UUID.randomUUID().toString().replace("-", "")
        redisClient.send(Request.cmd(Command.SETEX).arg(token).arg(expiresIn).arg(appId)).await()
        return ApiResponse(
            true,
            data = JsonObject().put("token", token).put("expiresIn", expiresIn)
        )
    }

    private suspend fun exists(appId: String, appSecret: String): Boolean {
        return client.preparedQuery("select 1 from tb_application where app_id=$1 and app_secret=$2")
            .execute(Tuple.of(appId, appSecret)).await().size() > 0
    }
}