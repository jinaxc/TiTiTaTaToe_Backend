package Server.Handler;

import Chess.User.Player;
import Server.TicTacServer;
import Server.Utils.RequestCode;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * @author jinaxCai
 */
public class NormalHttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final static Logger LOGGER = LogManager.getLogger(NormalHttpRequestHandler.class);
    private final TicTacServer server;
    public  NormalHttpRequestHandler(TicTacServer server) {
        this.server = server;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        LOGGER.info("receive normal http request,uri : {},method : {}",request.uri(),request.method().name());
        switch (request.method().name()){
            case "GET":
                handleGet(ctx,request);
            case "POST":
                handlePost(ctx,request);
            case "OPTIONS":
                handleOption(ctx,request);
            default:
                //TODO
        }
    }
    private void handleGet(ChannelHandlerContext ctx,FullHttpRequest msg){
        String path = getPath(ctx, msg);
        if(path == null){
            return;
        }
//        checkUriValidity(ctx, strings);
        switch (path){
            case "username":
                LOGGER.info("deal with get /username");
                Player player = server.getPlayerBySocketAddress(ctx.channel().remoteAddress());
                DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                        Unpooled.copiedBuffer("\"username\":" + player.getUsername(), CharsetUtil.UTF_8));
                defaultFullHttpResponse.headers()
                        .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
                        .set(HttpHeaderNames.CONTENT_TYPE, "application/json")
                        .set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE)
                        .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes());
                ctx.writeAndFlush(defaultFullHttpResponse);
            default:
                //TODO
        }
    }


    private void handlePost(ChannelHandlerContext ctx,FullHttpRequest msg){
        String path = getPath(ctx, msg);
        if(path == null){
            return;
        }
//        checkUriValidity(ctx, strings);
        switch (path){
            case "username":
                LOGGER.info("deal with post /username");
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
                            Unpooled.copiedBuffer("{\"success\":true}", CharsetUtil.UTF_8));
                }else{
                    defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                            Unpooled.copiedBuffer("{\"success\":false}", CharsetUtil.UTF_8));
                }
                defaultFullHttpResponse.headers()
                        .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
                        .set(HttpHeaderNames.CONTENT_TYPE, "application/json")
                        .set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE)
                        .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes());
                ctx.writeAndFlush(defaultFullHttpResponse);

            default:
                //TODO
        }
    }

    private void handleOption(ChannelHandlerContext ctx, FullHttpRequest msg){
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer("", CharsetUtil.UTF_8));
        defaultFullHttpResponse.headers()
                .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
                .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS,"*")
                .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS,true)
                .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS,"GET,PUT,POST")
                .set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE)
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes());

        ctx.writeAndFlush(defaultFullHttpResponse);
    }

    private String getPath(ChannelHandlerContext ctx, FullHttpRequest msg) {
        int index = msg.uri().lastIndexOf("/");
        if (index == -1) {
            DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND,
                    Unpooled.copiedBuffer("Uri Not Valid", CharsetUtil.UTF_8));
            defaultFullHttpResponse.headers()
                    .set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN,"*")
                    .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                    .set(HttpHeaderNames.CONTENT_LENGTH, defaultFullHttpResponse.content().readableBytes())
                    .set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE)
                    .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            ChannelFuture f = ctx.writeAndFlush(defaultFullHttpResponse);
            f.addListener(ChannelFutureListener.CLOSE);
            return null;
        }
        return msg.uri().substring(index + 1);
    }
}
