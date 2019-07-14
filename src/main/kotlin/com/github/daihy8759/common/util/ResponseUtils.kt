package com.github.daihy8759.common.util

import io.vertx.core.json.JsonObject

fun fail(): JsonObject {
    return JsonObject().put("success", false)
}

fun success(): JsonObject {
    return JsonObject().put("success", true)
}