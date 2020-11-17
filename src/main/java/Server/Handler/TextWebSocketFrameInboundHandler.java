package Server.Handler;

import Server.DataPackage.Packages;
import Server.TicTacServer;
import Server.Utils.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.concurrent.ScheduledFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author : chara
 */
public class TextWebSocketFrameInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final static Logger LOGGER = LogManager.getLogger(TextWebSocketFrameInboundHandler.class);
    private final TicTacServer server;
    private ScheduledFuture<?> scheduledFuture;

    public TextWebSocketFrameInboundHandler(TicTacServer server) {
        this.server = server;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler
                .HandshakeComplete) {
            LOGGER.info("connection success from {}",ctx.channel().remoteAddress());
            ctx.pipeline().addLast("gameHandler",new GameHandler((SocketChannel) ctx.channel(),server));
            scheduledFuture = ctx.pipeline().channel().eventLoop().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    LOGGER.info("auto sending users");
                    ctx.fireChannelRead(Unpooled.copiedBuffer((RequestCode.GET_USERS + "\n").getBytes()));
                }
            }, 3, 15, TimeUnit.SECONDS);
            ctx.writeAndFlush(new TextWebSocketFrame(Packages.ConnectionPackage().toString()));
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(scheduledFuture != null){
            scheduledFuture.cancel(false);
        }
        ctx.fireChannelInactive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        byteBuf.retain();
        LOGGER.info("get websocket data from {}",ctx.channel().remoteAddress());
        ctx.fireChannelRead(byteBuf);
    }
}