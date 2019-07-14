package com.github.daihy8759.common.util

import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

fun HttpServerResponse.endWithJson(obj: Any) {
    putHeader("Content-Type", "application/json; charset=utf-8").end(Json.encodePrettily(obj))
}

val RoutingContext.request: HttpServerRequest get() = request()

val RoutingContext.response: HttpServerResponse get() = response()

/**
 * Source annotation for verticles to bind unused rules to.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class VerticleClass