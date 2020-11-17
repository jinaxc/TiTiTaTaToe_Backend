package Server.Handler;

import Chess.Game;
import Chess.User.Player;
import Chess.Utils.DefaultGame;
import Server.DataPackage.Packages;
import Server.TicTacServer;
import Server.Utils.RequestCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author jinaxCai
 */
public class GameHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final static Logger LOGGER = LogManager.getLogger(GameHandler.class);
    private final SocketChannel[] channels;
    private Game game;
    private final TicTacServer server;
    public GameHandler(SocketChannel channel1, TicTacServer server) {
        this.server = server;
        this.channels = new SocketChannel[2];
        channels[0] = channel1;
    }

    public void connect(SocketChannel channel2,Game game){
        channels[1] = channel2;
        this.game = game;
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
            case RequestCode.PUT:
                handlePut(source, command, s[1]);
                break;
            case RequestCode.GET_USERS:{
                handleGetUsers(source, command);
                break;
            }
            case RequestCode.INVITE:{
                handleInvite(source, command, s);
                break;
            }
            case RequestCode.ANSWER_INVITE:{
                handleAnswerInvite(source, command, s);
                break;
            }
            case RequestCode.APPLY_USERNAME:{
                handleApplyUsername(source, s);
                break;
            }
            case RequestCode.GET_USERNAME:{
                Player player = server.getPlayerBySocketAddress(source.remoteAddress());
                if(player == null){
                    source.writeAndFlush(new TextWebSocketFrame(Packages.GetUsernamePackage(false,"not login yet").toString()));
                }else{
                    source.writeAndFlush(new TextWebSocketFrame(Packages.GetUsernamePackage(true,player.getUsername()).toString()));
                }
            }
            default://TODO
        }
    }

    private void handleApplyUsername(SocketChannel source, String[] s) {
        if(s.length < 2){
            source.writeAndFlush(new TextWebSocketFrame(Packages.InvalidRequestPackage().toString()));
        }
        String applyName = s[1];
        Player p = new Player(applyName,source);
        boolean b = server.addPlayer(p);
        if(b){
            source.writeAndFlush(new TextWebSocketFrame(Packages.ApplyUsernamePackage(true,applyName).toString()));
        }else{
            source.writeAndFlush(new TextWebSocketFrame(Packages.ApplyUsernamePackage(false,"name" + applyName + " already used").toString()));
        }
    }

    private void handleAnswerInvite(SocketChannel source, String command, String[] s) {
        LOGGER.info("receive message ANSWER_INVITE, message is {}",command);
        if(s.length < 2){
            source.writeAndFlush(new TextWebSocketFrame(Packages.InvalidRequestPackage().toString()));
        }else{
            int accept = Integer.parseInt(s[1].charAt(0) + "");
            String opponent = s[1].substring(1);
            Player current = server.getPlayerBySocketAddress(source.remoteAddress());
            Player player = server.getPlayerByUsername(opponent);
            //the opponent may already close the connection
            if(player == null){
                source.writeAndFlush(new TextWebSocketFrame(Packages.ConfirmAnswerInvitePackage(false,accept == 1,"").toString()));
            }else{
                if(accept == 1){
                    game = new DefaultGame();
                    channels[1] = player.getSocketChannel();
                    ((GameHandler)channels[1].pipeline().get("gameHandler")).connect(source,game);
                    ChannelFuture channelFuture = channels[1].writeAndFlush(Packages.AnswerInvitePackage(true, current.getUsername()));
                    channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                        @Override
                        public void operationComplete(Future<? super Void> future) throws Exception {
                            channels[0].writeAndFlush(Packages.GameStartPackage(0));
                            channels[1].writeAndFlush(Packages.GameStartPackage(0));
                        }
                    });
                }else{
                    channels[1].writeAndFlush(Packages.AnswerInvitePackage(false,current.getUsername()));
                }
            }
        }
    }

    private void handleInvite(SocketChannel source, String command, String[] s) {
        LOGGER.info("receive message INVITE, message is {}",command);
        if(s.length < 2){
            source.writeAndFlush(new TextWebSocketFrame(Packages.AnswerInvitePackage(false,"").toString()));
        }else{
            String opponent = s[1];
            Player current = server.getPlayerBySocketAddress(source.remoteAddress());
            Player player = server.getPlayerByUsername(opponent);
            if(player == null){
                source.writeAndFlush(new TextWebSocketFrame(Packages.AnswerInvitePackage(false,"").toString()));
            }else{
                player.getSocketChannel().writeAndFlush(new TextWebSocketFrame(Packages.ReceiveInvitePackage(current.getUsername()).toString()));
            }
        }
    }

    private void handleGetUsers(SocketChannel source, String command) {
        LOGGER.info("receive message GET_USERS, message is {}",command);
        StringBuilder result = new StringBuilder();
        for(Player player : server.getAllUsers()){
            result.append(player.getUsername()).append(",");
        }
        source.writeAndFlush(new TextWebSocketFrame(Packages.AllUsersPackage(result.toString()).toString()));
    }

    private void handlePut(SocketChannel source, String command, String place1) {
        LOGGER.info("receive message PUT, message is {}",command);
        if(channels[1] == null){
            source.writeAndFlush(new TextWebSocketFrame(Packages.FailPutPackage("game is not started").toString()));
        }
        int boardCount = place1.charAt(0) - '0';
        int x = place1.charAt(1) - '0';
        int y = place1.charAt(2) - '0';
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
            source.writeAndFlush(new TextWebSocketFrame(Packages.FailPutPackage("put failed").toString()));
        }else{
            int checkWin = game.checkWin();
            channels[0].writeAndFlush(new TextWebSocketFrame(Packages.SuccessPutPackage(player,game.getBoard(),game.getNextPos(),checkWin).toString()));
            channels[1].writeAndFlush(new TextWebSocketFrame(Packages.SuccessPutPackage(player,game.getBoard(),game.getNextPos(),checkWin).toString()));
        }
    }


}
