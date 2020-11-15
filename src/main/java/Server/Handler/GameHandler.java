package Server.Handler;

import Chess.Game;
import Chess.Utils.DefaultGame;
import Server.DataPackage.Packages;
import Server.Utils.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * @author jinaxCai
 */
public class GameHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger LOGGER = LogManager.getLogger(GameHandler.class);
    private SocketChannel[] channels;
    private Game game = new DefaultGame();

    public GameHandler(SocketChannel channel1,SocketChannel channel2) {
        this.channels = new SocketChannel[2];
        channels[0] = channel1;
        channels[1] = channel2;
    }

    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        CharSequence charSequence = msg.readCharSequence(msg.readableBytes(), CharsetUtil.UTF_8);
        String command = charSequence.toString();
        LOGGER.info("receive message from {}, message is {}",ctx.channel().remoteAddress(),command);
        handleCommand((SocketChannel) ctx.channel(),command);
    }

    private void handleCommand(SocketChannel source,String command){
        String[] s = command.split(" ");
        //need check validity
        int requestCode = Integer.parseInt(s[0]);
        switch (requestCode){
            case RequestCode.PUT:{
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
                    source.writeAndFlush(Packages.FailPutPackage());
                }else{
                    int checkWin = game.checkWin();
                    source.writeAndFlush(Packages.SuccessPutPackage(player,game.getBoard(),game.getNextPos(),checkWin));
                }
            }
            default://TODO
        }
    }


}
