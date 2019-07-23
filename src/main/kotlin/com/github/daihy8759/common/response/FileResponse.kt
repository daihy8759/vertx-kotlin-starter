package com.github.daihy8759.common.response

/**
 * @description file response
 * @author daihy
 * @version 1.0.0
 */
data class FileResponse(val filePath: String) {
    var contentType: String = "image/jpeg"

    constructor(filePath: String, contentType: String) : this(filePath) {
        this.contentType = contentType
    }
}
