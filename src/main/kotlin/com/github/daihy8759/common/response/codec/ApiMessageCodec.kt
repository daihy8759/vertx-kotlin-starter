package com.github.daihy8759.common.response.codec

import com.github.daihy8759.common.response.ApiResponse
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.JsonObject

/**
 * @description ApiResponse codec
 * @author daihy
 * @version 1.0.0
 */
class ApiMessageCodec : MessageCodec<ApiResponse, ApiResponse> {

    override fun decodeFromWire(position: Int, buffer: Buffer): ApiResponse {
        var pos = position
        val length = buffer.getInt(pos)
        var posKey: Int = pos + 4
        val jsonStr = buffer.getString(posKey, pos + length)
        val contentJson = JsonObject(jsonStr)
        val code = contentJson.getInteger("code")
        val success = contentJson.getBoolean("success")
        val message = contentJson.getString("message")
        val data = contentJson.getValue("data")
        return ApiResponse(success).apply {
            this.code = code
            this.message = message
            this.data = data
        }
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun encodeToWire(buffer: Buffer, apiResponse: ApiResponse) {
        val jsonToEncode = JsonObject()
        jsonToEncode.apply {
            put("code", apiResponse.code)
            put("success", apiResponse.success)
            put("message", apiResponse.message)
            put("data", apiResponse.data)
        }
        val jsonToStr = jsonToEncode.encode()
        val length = jsonToStr.toByteArray().size
        buffer.appendInt(length)
        buffer.appendString(jsonToStr)
    }

    override fun transform(apiResponse: ApiResponse): ApiResponse {
        return apiResponse
    }

    override fun name(): String {
        return this.javaClass.name
    }
}
