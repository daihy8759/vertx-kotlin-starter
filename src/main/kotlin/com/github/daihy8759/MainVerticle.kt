package com.github.daihy8759

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.github.daihy8759.api.ApiVerticle
import com.github.daihy8759.common.response.ApiResponse
import com.github.daihy8759.common.response.BufferResponse
import com.github.daihy8759.common.response.FileResponse
import com.github.daihy8759.common.response.codec.ApiMessageCodec
import com.github.daihy8759.common.response.codec.BufferMessageCodec
import com.github.daihy8759.common.response.codec.FileMessageCodec
import com.github.daihy8759.verticle.TokenVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.Json
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions

class MainVerticle : CoroutineVerticle() {

    override suspend fun start() {
        var eventBus = vertx.eventBus()
        eventBus.registerDefaultCodec(ApiResponse::class.java, ApiMessageCodec())
        eventBus.registerDefaultCodec(BufferResponse::class.java, BufferMessageCodec())
        eventBus.registerDefaultCodec(FileResponse::class.java, FileMessageCodec())
        Json.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val databaseConfig = config.getJsonObject("database")
        val connectOptions = PgConnectOptions().setHost(databaseConfig.getString("host"))
            .setPort(databaseConfig.getInteger("port")!!).setDatabase(databaseConfig.getString("database"))
            .setUser(databaseConfig.getString("username")).setPassword(databaseConfig.getString("password"))
        val poolOptions = PoolOptions().setMaxSize(databaseConfig.getInteger("maxPoolSize")!!)
        val pool = PgPool.pool(vertx, connectOptions, poolOptions)

        vertx.deployVerticleAwait(ApiVerticle(), DeploymentOptions().setConfig(config))
        vertx.deployVerticleAwait(TokenVerticle(pool), DeploymentOptions().setConfig(config))
    }
}