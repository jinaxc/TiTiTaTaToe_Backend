package Server.Handler;

import Server.TicTacServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

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
        List<SocketChannel> channels = server.getChannels();
        channels.remove(ctx.channel());
        LOGGER.info("break connection with {}",ctx.channel());
        ctx.fireChannelInactive();
    }
}
