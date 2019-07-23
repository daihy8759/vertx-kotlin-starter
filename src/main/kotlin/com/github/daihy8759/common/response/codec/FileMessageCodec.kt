package com.github.daihy8759.common.response.codec

import com.github.daihy8759.common.response.FileResponse
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.JsonObject

/**
 * @description FileResponse code
 * @author daihy
 * @version 1.0.0
 */
class FileMessageCodec : MessageCodec<FileResponse, FileResponse> {

    override fun decodeFromWire(position: Int, buffer: Buffer): FileResponse {
        var pos = position
        val length = buffer.getInt(pos)
        var posKey: Int = pos + 4
        val jsonStr = buffer.getString(posKey, pos + length)
        val contentJson = JsonObject(jsonStr)
        val filePath = contentJson.getString("filePath")
        val contentType = contentJson.getString("contentType")
        return FileResponse(filePath, contentType)
    }

    override fun systemCodecID(): Byte {
        return -1
    }

    override fun encodeToWire(buffer: Buffer, fileResponse: FileResponse) {
        val jsonToEncode = JsonObject()
        jsonToEncode.apply {
            put("filePath", fileResponse.filePath)
            put("contentType", fileResponse.contentType)
        }
        val jsonToStr = jsonToEncode.encode()
        val length = jsonToStr.toByteArray().size
        buffer.appendInt(length)
        buffer.appendString(jsonToStr)
    }

    override fun transform(fileResponse: FileResponse): FileResponse {
        return fileResponse
    }

    override fun name(): String {
        return this.javaClass.name
    }
}
