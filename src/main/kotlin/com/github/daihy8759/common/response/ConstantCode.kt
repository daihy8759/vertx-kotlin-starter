package com.github.daihy8759.common.response

object ConstantCode {

    val SUCCESS = RetCode(0, "success")
    val FAIL = RetCode(-1, "fail")

    val SYSTEM_ERROR = RetCode(10000, "系统异常！")
    val EMPTY_PARAM = RetCode(10001, "参数错误！")
    val SYSTEM_DATA_NOT_EXISTS = RetCode(10002, "数据不存在！")
    val DATABASE_NOT_SUPPORT = RetCode(10003, "数据库配置错误！")
    val CREATE_SIGN_IMAGE_ERROR = RetCode(10004, "创建签名图片失败")
    val CREATE_DIF_ERROR = RetCode(10005, "创建文件夹失败")

    val TOKEN_NOT_FOUND = RetCode(22035, "token not found！")
    val TOKEN_REQUIRE_FAIL = RetCode(22036, "app_id or secret is empty！")
    val LOGIN_FAIL = RetCode(22037, "login fail")
}
