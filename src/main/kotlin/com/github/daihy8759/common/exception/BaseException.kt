package com.notary.brand.common.exception

import com.github.daihy8759.common.response.RetCode


class BaseException : RuntimeException {

    var retCode: RetCode

    constructor(retCode: RetCode) {
        this.retCode = retCode
    }

    constructor(code: Int, msg: String) : this(RetCode(code, msg)) {
    }

}