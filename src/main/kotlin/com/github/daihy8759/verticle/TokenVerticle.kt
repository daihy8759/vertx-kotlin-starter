package com.github.daihy8759.verticle

import com.github.daihy8759.common.util.Constants
import com.github.daihy8759.common.util.VerticleClass
import com.github.daihy8759.common.util.fail
import com.github.daihy8759.common.util.success
import com.github.daihy8759.keys.TokenKey
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.redis.setexAwait
import io.vertx.kotlin.sqlclient.preparedQueryAwait
import io.vertx.redis.RedisClient
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.util.*


@VerticleClass
class TokenVerticle(val client: SqlClient) : CoroutineBaseVerticle() {

    private lateinit var redisClient: RedisClient
    private lateinit var constantConfig: JsonObject

    override suspend fun start() {
        constantConfig = config.getJsonObject("constant");
        redisClient = RedisClient.create(vertx, config.getJsonObject("redis"))

        vertx.eventBus().coroutineConsumer(TokenKey.GET_TOKEN, this::getToken);
    }

    private suspend fun getToken(message: Message<JsonObject>) {
        val paramObject = message.body().getJsonObject(Constants.REQUEST_PARAM)
        val appId :String? = paramObject.getString("appId")
        val appSecret:String? = paramObject.getString("appSecret")
        if(appId.isNullOrBlank() || appSecret.isNullOrBlank()){
            message.reply(fail().put("message", "app_id or secret is empty！"))
            return;
        }
        if (exists(appId, appSecret)) {
            message.reply(createToken(appId))
        } else {
            message.reply(fail().put("message", "app_id or secret not correct！"))
        }
    }

    private suspend fun createToken(appId: String): JsonObject {
        val expiresIn = constantConfig.getLong("tokenExpire", 1200L)
        val token = UUID.randomUUID().toString().replace("-", "")
        redisClient.setexAwait(token, expiresIn, appId)
        return success().put("data", JsonObject().put("token", token).put("expiresIn", expiresIn))
    }

    private suspend fun exists(appId: String, appSecret: String): Boolean {
        return client.preparedQueryAwait("select 1 from tb_application where app_id=$1 and app_secret=$2",
                Tuple.of(appId, appSecret)).size() > 0
    }
}