package Server;

import Server.Handler.ChannelInactiveHandler;
import Server.Handler.HttpRequestHandler;
import Server.Handler.TextWebSocketFrameInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jinaxCai
 */
public class TicTacServer {
    private final List<SocketChannel> channels;

    public static void main(String[] args) throws InterruptedException {
        new TicTacServer().start();
//
    }

    public TicTacServer() {
        channels = Collections.synchronizedList(new ArrayList<SocketChannel>());
    }

    public List<SocketChannel> getChannels() {
        return channels;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            final TicTacServer s = this;
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ChannelInactiveHandler(s));
//                            pipeline.addLast(new IdleStateHandler(60,60, 0));
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast(new HttpRequestHandler("/ws"));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true));
                            pipeline.addLast(new TextWebSocketFrameInboundHandler(s));
                            pipeline.addLast(new LineBasedFrameDecoder(1024));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(8888).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

