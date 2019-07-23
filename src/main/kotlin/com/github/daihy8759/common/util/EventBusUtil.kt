package com.github.daihy8759.common.util

import com.github.daihy8759.common.response.ApiResponse
import com.notary.brand.common.exception.BaseException
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.apache.logging.log4j.LogManager

class EventBusUtil {

    companion object {

        private val log = LogManager.getLogger()

        fun catchException(e: Throwable, message: Message<JsonObject>) {
            if (e is BaseException) {
                message.reply(ApiResponse(false, e.retCode))
                return
            }
            log.error(getStackTrace(e))
            message.reply(ApiResponse(false, "服务器异常"))
        }
    }
}
