package Server.Handler;

import Server.DataPackage.Packages;
import Server.TicTacServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class TextWebSocketFrameInboundHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final static Logger LOGGER = LogManager.getLogger(TextWebSocketFrameInboundHandler.class);
    private final TicTacServer server;

    public TextWebSocketFrameInboundHandler(TicTacServer server) {
        this.server = server;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler
                .HandshakeComplete) {
            ctx.pipeline().remove(HttpRequestHandler.class);
            server.getChannels().add((SocketChannel) ctx.channel());
            LOGGER.info("connection success from {}",ctx.channel().remoteAddress());
            ctx.writeAndFlush(new TextWebSocketFrame(Packages.ConnectionPackage().toString()));
            SocketChannel c1 = null;
            SocketChannel c2 = null;
            synchronized (server.getChannels()) {
                if (server.getChannels().size() == 2) {
                    c1 = server.getChannels().get(0);
                    c2 = server.getChannels().get(1);
                }
            }
            if(c1 != null && c2 != null){
                GameHandler gameHandler = new GameHandler(c1,c2);
                c1.pipeline().addLast(gameHandler);
                c2.pipeline().addLast(gameHandler);
                c1.writeAndFlush(new TextWebSocketFrame(Packages.GameStartPackage(0).toString()));
                c2.writeAndFlush(new TextWebSocketFrame(Packages.GameStartPackage(1).toString()));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        ByteBuf byteBuf = msg.content();
        byteBuf.retain();
        LOGGER.info("get websocket data from {}",ctx.channel().remoteAddress());
        ctx.fireChannelRead(byteBuf);
    }
}