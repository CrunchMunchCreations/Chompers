package xyz.bluspring.sprinkles.util

import java.net.URLEncoder
import java.net.http.HttpRequest

object HttpHelper {
    fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

    fun formData(data: Map<String, String>): HttpRequest.BodyPublisher? {
        val res = data.map {(k, v) -> "${(k.utf8())}=${v.utf8()}"}
            .joinToString("&")

        return HttpRequest.BodyPublishers.ofString(res)
    }
}