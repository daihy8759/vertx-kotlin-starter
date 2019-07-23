package com.github.daihy8759.common.response

data class ApiResponse(val success: Boolean) {
    var code: Int = 0
    var message: String? = null
    var data: Any? = null

    constructor(success: Boolean, message: String?) : this(success) {
        this.message = message
    }

    constructor(success: Boolean, data: Any?) : this(success) {
        this.data = data
    }

    constructor(success: Boolean, retCode: RetCode) : this(success) {
        this.code = retCode.code
        this.message = retCode.msg
    }
}

data class RetCode(val code: Int, val msg: String)