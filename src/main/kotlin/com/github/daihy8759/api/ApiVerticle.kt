package com.github.daihy8759.api

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
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.redis.RedisClient
import org.apache.logging.log4j.LogManager

@VerticleClass
class ApiVerticle() : CoroutineBaseVerticle() {

    val log = LogManager.getLogger()

    private val defaultHost = "localhost"
    private val defaultPort = 18087
    private lateinit var noAuthUrl: JsonArray
    private lateinit var tokenService: TokenService

    override suspend fun start() {
        var apiObject = config.getJsonObject("api")
        noAuthUrl = apiObject.getJsonArray("noAuthUrl")

        val redisClient = RedisClient.create(vertx, config.getJsonObject("redis"))
        this.tokenService = TokenService(redisClient)

        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.route("/api/*").coroutineHandler(this::handleApi)

        val host = config.getString("api.http.address", defaultHost)
        val port = config.getInteger("api.http.port", defaultPort)
        vertx.createHttpServer().requestHandler(router).listenAwait(port, host)
    }

    private suspend fun handleApi(routingContext: RoutingContext) {
        val requestPath = routingContext.request().path()
        routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
        if (requestPath.length < 7) {
            routingContext.response.endWithJson(fail().put("message", "handler not exists!"))
            return
        }
        val apiVersion = requestPath.substring(5, 7)
        val eventBusKey = requestPath.substring(7)

        if (noAuthUrl.contains(eventBusKey)) {
            sendEvent(routingContext, eventBusKey, apiVersion)
        } else {
            var result = checkToken(routingContext.request())
            if (result) {
                sendEvent(routingContext, eventBusKey, apiVersion)
            } else {
                routingContext.response().endWithJson(success());
            }
        }
    }

    private suspend fun sendEvent(routingContext: RoutingContext, eventBusKey: String, apiVersion: String) {
        val response = routingContext.response
        try {
            var message = vertx.eventBus().requestAwait<JsonObject>(
                    eventBusKey,
                    parseParam(routingContext, JsonObject().put(Constants.API_VERSION, apiVersion)))
            response.endWithJson(message.body())
        } catch (e: Exception) {
            log.error(e)
            response.endWithJson(fail().put("message", e.message))
        }
    }

    private fun getToken(request: HttpServerRequest): String {
        val token: String? = request.getHeader(Constants.AUTH_HEADER)
        log.debug("token: {}", token)
        if (token.isNullOrBlank()) {
            return ""
        }
        return token.trim().replace(":", "")
    }

    private suspend fun checkToken(request: HttpServerRequest): Boolean {
        var token = getToken(request)
        if (token.isNullOrBlank()) {
            return false
        }
        if (tokenService.getToken(token)!!.isNotBlank()) {
            return true
        }
        return false
    }

    private fun parseParam(routingContext: RoutingContext, extParam: JsonObject): JsonObject {
        val paramsMap = routingContext.request().params()
        val paramObject = JsonObject()
        extParam.put(Constants.TOKEN, getToken(routingContext.request()))
        extParam.put(Constants.REQUEST_PARAM, paramObject)
        paramsMap.entries().forEach { t -> paramObject.put(t.key, t.value) }
        val contentType = routingContext.request().getHeader("Content-Type")
        if (contentType.isNotBlank() && contentType.startsWith("application/json")) {
            extParam.put(Constants.REQUEST_BODY, routingContext.bodyAsJson)
        }
        return extParam
    }
}