package xyz.bluspring.sprinkles.twitch.auth.callback

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import xyz.bluspring.sprinkles.twitch.SprinklesTwitch
import xyz.bluspring.sprinkles.twitch.auth.TwitchUserAuth
import java.net.URL

object TwitchCallbackServer {
    private val logger = LoggerFactory.getLogger(TwitchCallbackServer::class.java)

    private var running = false
    private var future: ChannelFuture? = null

    fun start() {
        if (running)
            return

        // TODO: how the fuck do i actually do this
        runBlocking {
            launch {
                startServer()
            }
        }
    }

    fun handleParams(params: Map<String, List<String>>): Boolean {
        if (!params.contains("code"))
            return false

        val code = params["code"]!!

        return try {
            TwitchUserAuth.authorizeAccessToken(code[0])
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stopServer() {
        logger.info("Shutting down callback server")
        future?.channel()?.close()?.sync()
        future?.channel()?.parent()?.close()?.sync()
        future = null
    }

    private fun startServer() {
        val url = URL(SprinklesTwitch.instance.config.redirectUri)

        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()

        try {
            val b = ServerBootstrap()
            b.apply {
                group(bossGroup, workerGroup)
                channel(NioServerSocketChannel::class.java)
                childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline()
                            .addLast(
                                HttpRequestDecoder(),
                                HttpResponseEncoder(),
                                TwitchHttpServerHandler()
                            )
                    }
                })
                option(ChannelOption.SO_BACKLOG, 128)
                childOption(ChannelOption.SO_KEEPALIVE, true)
            }

            val f = b.bind(url.host, url.port).sync()

            logger.info("Temporarily opened callback server on $url")
            running = true
            future = f

            f.channel().closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
            running = false

            logger.info("Shut down callback server!")
        }
    }
}