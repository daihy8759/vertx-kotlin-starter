package com.github.daihy8759.common.response

import io.vertx.core.buffer.Buffer

/**
 * 响应图片请求
 */
data class BufferResponse(val buffer: Buffer) {
    var contentType: String = "image/jpeg"

    constructor(buffer: Buffer, contentType: String) : this(buffer) {
        this.contentType = contentType
    }
}
