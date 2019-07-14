package com.github.daihy8759.verticle

import com.github.daihy8759.common.util.fail
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch

open class CoroutineBaseVerticle : CoroutineVerticle() {

    protected fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }

    protected fun <T> EventBus.coroutineConsumer(address: String, fn: suspend (Message<T>) -> Unit) {
        this.consumer<T>(address) { ctx ->
            launch(vertx.dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.reply(fail().put("message", e.message))
                }
            }
        }
    }
}