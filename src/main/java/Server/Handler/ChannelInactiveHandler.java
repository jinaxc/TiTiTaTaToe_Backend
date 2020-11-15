package Server.Handler;

import Server.TicTacServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author : chara
 */
public class ChannelInactiveHandler extends ChannelInboundHandlerAdapter {
    private final static Logger LOGGER = LogManager.getLogger(ChannelInactiveHandler.class);
    private final TicTacServer server;

    public ChannelInactiveHandler(TicTacServer server) {
        this.server = server;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        server.removePlayerBySocketAddress(ctx.channel().remoteAddress());
        LOGGER.info("break connection with {}",ctx.channel());
        ctx.fireChannelInactive();
    }
}
