package xyz.bluspring.sprinkles.twitch.auth.callback
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil
import java.util.*


// https://www.baeldung.com/java-netty-http-server
object RequestUtils {
    fun formatParams(request: HttpRequest): StringBuilder {
        val responseData = StringBuilder()
        val queryStringDecoder = QueryStringDecoder(request.uri())
        val params: Map<String, List<String>> = queryStringDecoder.parameters()

        if (params.isNotEmpty()) {
            params.forEach { (name, values) ->
                values.forEach {
                    responseData
                        .append("Parameter: ")
                        .append(name.uppercase())
                        .append(" = ")
                        .append(it.uppercase())
                        .append("\r\n")
                }
            }

            responseData.append("\r\n")
        }
        return responseData
    }

    fun formatBody(httpContent: HttpContent): StringBuilder {
        val responseData = StringBuilder()
        val content = httpContent.content()

        if (content.isReadable) {
            responseData
                .append(content.toString(CharsetUtil.UTF_8).uppercase(Locale.getDefault()))
                .append("\r\n")
        }

        return responseData
    }

    fun evaluateDecoderResult(o: HttpObject): StringBuilder {
        val responseData = StringBuilder()
        val result = o.decoderResult()
        if (!result.isSuccess) {
            responseData.append("..Decoder Failure: ")
            responseData.append(result.cause())
            responseData.append("\r\n")
        }
        return responseData
    }

    fun prepareLastResponse(request: HttpRequest?, trailer: LastHttpContent, isSuccess: Boolean): StringBuilder {
        val responseData = StringBuilder()

        val resp = if (isSuccess)
            "Successfully logged into Twitch! You can now safely close this window."
        else
            "Failed to log into Twitch! Please try again."

        responseData.append("$resp\r\n")

        return responseData
    }
}