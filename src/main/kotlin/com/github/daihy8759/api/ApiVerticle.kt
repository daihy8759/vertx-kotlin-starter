package com.github.daihy8759.api

import com.github.daihy8759.common.response.ApiResponse
import com.github.daihy8759.common.response.BufferResponse
import com.github.daihy8759.common.response.ConstantCode
import com.github.daihy8759.common.response.FileResponse
import com.github.daihy8759.common.util.*
import com.github.daihy8759.service.TokenService
import com.github.daihy8759.verticle.CoroutineBaseVerticle
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisOptions
import org.apache.logging.log4j.LogManager

@VerticleClass
class ApiVerticle : CoroutineBaseVerticle() {

    val log = LogManager.getLogger()

    private val defaultHost = "localhost"
    private val defaultPort = 18087
    private lateinit var noAuthUrl: JsonArray
    private lateinit var tokenService: TokenService

    override suspend fun start() {
        val apiObject = config.getJsonObject("api")
        noAuthUrl = apiObject.getJsonArray("noAuthUrl")

        val redisClient = Redis.createClient(vertx, RedisOptions(config.getJsonObject("redis")))
        this.tokenService = TokenService(redisClient)

        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.route("/api/*").coroutineHandler(this::handleApi)

        val host = config.getString("api.http.address", defaultHost)
        val port = config.getInteger("api.http.port", defaultPort)
        vertx.createHttpServer().requestHandler(router).listen(port, host).await()
    }

    private suspend fun handleApi(routingContext: RoutingContext) {
        val requestPath = routingContext.request().path()
        if (requestPath.length < 7) {
            routingContext.response.endWithJson(ApiResponse(false, "handler not exists!"))
            return
        }
        val apiVersion = requestPath.substring(5, 7)
        val eventBusKey = requestPath.substring(7)

        if (noAuthUrl.contains(eventBusKey)) {
            sendEvent(routingContext, eventBusKey, apiVersion, null)
        } else {
            val userInfo = checkToken(routingContext.request())
            if (userInfo.isNullOrBlank()) {
                routingContext.response().endWithJson(ApiResponse(false,
                        ConstantCode.TOKEN_NOT_FOUND))
            } else {
                sendEvent(routingContext, eventBusKey, apiVersion, userInfo)
            }
        }
    }

    private suspend fun sendEvent(routingContext: RoutingContext, eventBusKey: String,
                                  apiVersion: String, userInfo: String?) {
        val response = routingContext.response
        try {
            val message = vertx.eventBus().request<Any>(
                    eventBusKey,
                    parseParam(routingContext, JsonObject().put(Constants.API_VERSION, apiVersion)
                            .put(Constants.USER_INFO, userInfo))).await()
            when (val replyBody = message.body()) {
                is BufferResponse -> response
                        .putHeader(HttpHeaders.CONTENT_TYPE, replyBody.contentType)
                        .end(replyBody.buffer)
                is FileResponse -> response
                        .putHeader(HttpHeaders.CONTENT_TYPE, replyBody.contentType)
                        .end(vertx.fileSystem().readFile(replyBody.filePath).await())
                else -> {
                    response.endWithJson(replyBody)
                }
            }
        } catch (e: Exception) {
            log.error(getStackTrace(e))
            response.endWithJson(ApiResponse(false, e.message.orEmpty()))
        }
    }

    private fun getToken(request: HttpServerRequest): String {
        val token: String = request.getHeader(Constants.AUTH_HEADER)
                .orEmpty()
                .ifEmpty { request.getParam("token").orEmpty() }
        return token.trim().replace(":", "")
    }

    private suspend fun checkToken(request: HttpServerRequest): String? {
        val token = getToken(request)
        if (token.isBlank()) {
            return ""
        }
        return tokenService.getToken(token)
    }

    private fun parseParam(routingContext: RoutingContext, extParam: JsonObject): JsonObject {
        val paramsMap = routingContext.request.params()
        val paramObject = JsonObject()
        extParam.put(Constants.TOKEN, getToken(routingContext.request()))
        extParam.put(Constants.REQUEST_PARAM, paramObject)
        paramsMap.entries().forEach { t -> paramObject.put(t.key, t.value) }
        val contentType = routingContext.request().getHeader(HttpHeaders.CONTENT_TYPE)
        if (contentType.isNotBlank() && contentType.startsWith("application/json")) {
            extParam.put(Constants.REQUEST_BODY, routingContext.bodyAsJson)
        }
        return extParam
    }
}