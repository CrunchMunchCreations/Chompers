package xyz.bluspring.sprinkles.twitch.auth.callback
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil

// https://www.baeldung.com/java-netty-http-server
class TwitchHttpServerHandler : SimpleChannelInboundHandler<HttpObject>() {
    private var request: HttpRequest? = null
    val responseData = StringBuilder()

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        ctx.close()
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        if (msg is HttpRequest) {
            this.request = msg

            if (HttpUtil.is100ContinueExpected(msg)) {
                writeResponse(ctx)
            }

            responseData.setLength(0)
            responseData.append(RequestUtils.formatParams(msg))
        }

        responseData.append(RequestUtils.evaluateDecoderResult(this.request!!))

        if (msg is HttpContent) {
            responseData.append(RequestUtils.formatBody(msg))
            responseData.append(RequestUtils.evaluateDecoderResult(this.request!!))

            if (msg is LastHttpContent) {
                val queryStringDecoder = QueryStringDecoder(this.request!!.uri())
                val params: Map<String, List<String>> = queryStringDecoder.parameters()
                val handled = TwitchCallbackServer.handleParams(params)

                responseData.append(RequestUtils.prepareLastResponse(this.request, msg, handled))

                writeResponse(ctx, msg, responseData)

                if (handled)
                    TwitchCallbackServer.stopServer()
            }
        }
    }

    private fun writeResponse(ctx: ChannelHandlerContext) {
        val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER)
        ctx.write(response)
    }

    private fun writeResponse(
        ctx: ChannelHandlerContext, trailer: LastHttpContent,
        responseData: StringBuilder
    ) {
        val keepAlive = HttpUtil.isKeepAlive(request)

        val httpResponse: FullHttpResponse = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            if ((trailer as HttpObject).decoderResult().isSuccess) HttpResponseStatus.OK else HttpResponseStatus.BAD_REQUEST,
            Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8)
        )

        httpResponse.headers()[HttpHeaderNames.CONTENT_TYPE] = "text/plain; charset=UTF-8"

        if (keepAlive) {
            httpResponse.headers().setInt(
                HttpHeaderNames.CONTENT_LENGTH,
                httpResponse.content().readableBytes()
            )

            httpResponse.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
        }

        ctx.write(httpResponse)
        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
        }
    }
}