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
    public HttpRequestHandler(String s, TicTacServer server) {
        this.wsUri = s;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.uri())) {
            LOGGER.info("receive webSocket handshake request");
            ctx.fireChannelRead(request.retain());
        }else{
            LOGGER.info("receive normal http request,uri : {},method : {}",request.uri(),request.method().name());
            //TODO
        }
    }

}
