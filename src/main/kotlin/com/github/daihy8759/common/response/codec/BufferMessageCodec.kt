package com.github.daihy8759.common.response.codec

import com.github.daihy8759.common.response.BufferResponse
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.JsonObject

/**
 * @description BufferResponse codec
 * @author daihy
 * @version 1.0.0
 */
class BufferMessageCodec : MessageCodec<BufferResponse, BufferResponse> {

    override fun decodeFromWire(position: Int, buffer: Buffer): BufferResponse {
        var pos = position
        val length = buffer.getInt(pos)
        var posKey: Int = pos + 4
        val jsonStr = buffer.getString(posKey, pos + length)
        val contentJson = JsonObject(jsonStr)
        val bufferArray = contentJson.getBinary("buffer")
        val contentType = contentJson.getString("contentType")
        return BufferResponse(Buffer.buffer(bufferArray), contentType)
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun encodeToWire(buffer: Buffer, bufferResponse: BufferResponse) {
        val jsonToEncode = JsonObject()
        jsonToEncode.apply {
            put("buffer", bufferResponse.buffer)
            put("contentType", bufferResponse.contentType)
        }
        val jsonToStr = jsonToEncode.encode()
        val length = jsonToStr.toByteArray().size
        buffer.appendInt(length)
        buffer.appendString(jsonToStr)
    }

    override fun transform(bufferResponse: BufferResponse): BufferResponse {
        return bufferResponse
    }

    override fun name(): String {
        return this.javaClass.name
    }
}
