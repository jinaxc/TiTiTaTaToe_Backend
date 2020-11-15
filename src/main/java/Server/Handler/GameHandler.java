package Server.Handler;

import Chess.Game;
import Chess.Utils.DefaultGame;
import Server.DataPackage.Packages;
import Server.Utils.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.attribute.UserDefinedFileAttributeView;


/**
 * @author jinaxCai
 */
@ChannelHandler.Sharable
public class GameHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger LOGGER = LogManager.getLogger(GameHandler.class);
    private final SocketChannel[] channels;
    private final Game game = new DefaultGame();

    public GameHandler(SocketChannel channel1,SocketChannel channel2) {
        this.channels = new SocketChannel[2];
        channels[0] = channel1;
        channels[1] = channel2;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(ctx.channel() == channels[0]){
            channels[1].close();
            //TODO
        }else{
            channels[0].close();
            //TODO
        }
        ctx.fireChannelInactive();
    }

    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        CharSequence charSequence = msg.readCharSequence(msg.readableBytes(), CharsetUtil.UTF_8);
        String command = charSequence.toString();

        handleCommand((SocketChannel) ctx.channel(),command);
    }

    private void handleCommand(SocketChannel source,String command){
        String[] s = command.split(" ");
        //need check validity
        int requestCode = Integer.parseInt(s[0]);
        switch (requestCode){
            case RequestCode.PUT:{
                LOGGER.info("receive message PUT, message is {}",command);
                String place = s[1];
                int boardCount = place.charAt(0) - '0';
                int x = place.charAt(1) - '0';
                int y = place.charAt(2) - '0';
                int player;
                if(source == channels[0]){
                    player = 0;
                }else if(source == channels[1]){
                    player = 1;
                }else{
                    player = 0;
                    //TODO
                }
                boolean put = game.put(boardCount, x, y, player);
                if(!put){
                    source.writeAndFlush(new TextWebSocketFrame(Packages.FailPutPackage().toString()));
                }else{
                    int checkWin = game.checkWin();
                    channels[0].writeAndFlush(new TextWebSocketFrame(Packages.SuccessPutPackage(player,game.getBoard(),game.getNextPos(),checkWin).toString()));
                    channels[1].writeAndFlush(new TextWebSocketFrame(Packages.SuccessPutPackage(player,game.getBoard(),game.getNextPos(),checkWin).toString()));
                }
            }
            default://TODO
        }
    }


}
