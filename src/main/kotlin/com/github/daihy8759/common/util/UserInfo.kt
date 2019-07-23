package com.github.daihy8759.common.util

import com.github.daihy8759.common.util.Constants.Companion.USER_INFO
import com.github.daihy8759.common.util.Constants.Companion.USER_INFO_ID
import io.vertx.core.json.JsonObject

/**
 * @description TODO
 * @author daihy
 * @version 1.0.0
 */
class UserInfo {

    companion object {
        fun getUserInfo(param: JsonObject): JsonObject {
            val userInfo = param.getString(USER_INFO)
            return JsonObject(userInfo)
        }

        fun getUserId(param: JsonObject): String {
            val userInfo = param.getString(USER_INFO)
            return JsonObject(userInfo).getString(USER_INFO_ID)
        }

        fun getAppId(param: JsonObject): String {
            val userInfo = param.getString(USER_INFO)
            return JsonObject(userInfo).getString(Constants.USER_INFO_APP_ID)
        }
    }
}