package Server.Handler;

import Chess.User.Player;
import Server.TicTacServer;
import Server.Utils.RequestCode;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * @author : chara
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final static Logger LOGGER = LogManager.getLogger(HttpRequestHandler.class);
    private final String wsUri;
    private final TicTacServer server;
    public HttpRequestHandler(String s, TicTacServer server) {
        this.wsUri = s;
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.uri())) {
            LOGGER.info("receive webSocket handshake request");
            ctx.fireChannelRead(request.retain());
        }else{
            LOGGER.info("receive normal http request");
            switch (request.method().name()){
                case "GET":
                    handleGet(ctx,request);
                case "PUT":
                    handlePut(ctx,request);
                default:
                    //TODO
            }
        }
    }
    private void handleGet(ChannelHandlerContext ctx,FullHttpRequest msg){
        String[] strings = msg.uri().split("/");
        checkUriValidity(ctx, strings);
        switch (strings[1]){
            case "/username":
                Player player = server.getPlayerBySocketAddress(ctx.channel().remoteAddress());
                DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                        Unpooled.copiedBuffer("username:" + player.getUsername(), CharsetUtil.UTF_8));
                defaultFullHttpResponse.headers()
                        .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                        .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes());
                ctx.writeAndFlush(defaultFullHttpResponse);
            default:
                //TODO
        }
    }

    private void checkUriValidity(ChannelHandlerContext ctx, String[] strings) {
        if(strings.length < 2){
            DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                    Unpooled.copiedBuffer("Uri Not Valid", CharsetUtil.UTF_8));
            defaultFullHttpResponse.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes())
                    .set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);

            ChannelFuture f = ctx.writeAndFlush(defaultFullHttpResponse);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handlePut(ChannelHandlerContext ctx,FullHttpRequest msg){
        String[] strings = msg.uri().split("/");
        checkUriValidity(ctx, strings);
        switch (strings[1]){
            case "/username":
                String username = msg.content().readCharSequence(msg.content().readableBytes(), CharsetUtil.UTF_8).toString();
                Player player = new Player(username, (SocketChannel) ctx.channel());
                boolean b = server.addPlayer(player);
                ctx.channel().eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        TextWebSocketFrame frame = new TextWebSocketFrame(RequestCode.GET_USERS + "");
                        ctx.fireChannelRead(frame);
                    }
                },15, TimeUnit.SECONDS);
                DefaultFullHttpResponse defaultFullHttpResponse;
                if(b){
                    defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                            Unpooled.copiedBuffer("{success:true}", CharsetUtil.UTF_8));
                }else{
                    defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                            Unpooled.copiedBuffer("{success:false}", CharsetUtil.UTF_8));
                }
                defaultFullHttpResponse.headers()
                        .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                        .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes());
                ctx.writeAndFlush(defaultFullHttpResponse);

            default:
                //TODO
        }
    }
}
