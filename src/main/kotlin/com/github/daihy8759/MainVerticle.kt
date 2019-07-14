package com.github.daihy8759

import com.github.daihy8759.api.ApiVerticle
import com.github.daihy8759.verticle.TokenVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions

class MainVerticle() : CoroutineVerticle() {

    override suspend fun start() {
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