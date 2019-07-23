package com.github.daihy8759.common.util

import java.io.PrintWriter
import java.io.StringWriter

/**
 * @author daihy
 * @version 1.0.0
 */
fun getStackTrace(e: Throwable): String {
    val sw = StringWriter()
    e.printStackTrace(PrintWriter(sw))
    return sw.toString()
}
