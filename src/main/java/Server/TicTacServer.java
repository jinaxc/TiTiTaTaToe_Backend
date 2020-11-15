package Server;

import Chess.User.Player;
import Server.Handler.ChannelInactiveHandler;
import Server.Handler.HttpRequestHandler;
import Server.Handler.TextWebSocketFrameInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.SocketAddress;
import java.util.*;

/**
 * @author jinaxCai
 */
public class TicTacServer {
    private final Map<String, Player> playersIndexedByUsername;
    private final Map<SocketAddress, Player> playersIndexedBySocketChannel;
    private final Object lock;

    public static void main(String[] args) throws InterruptedException {
        new TicTacServer().start();
//
    }

    public TicTacServer() {
        playersIndexedByUsername = new HashMap<>();
        playersIndexedBySocketChannel = new HashMap<>();
        lock = new Object();
    }

    /**
     *
     * @return a list contain all the players
     */
    public List<Player> getAllUsers(){
        return new ArrayList<>(playersIndexedByUsername.values());
    }

    /**
     * concurrency safe
     * @param socketAddress socketAddress
     * @return true if removed, false if not existed
     */
    public boolean removePlayerBySocketAddress(SocketAddress socketAddress){
        synchronized (lock){
            Player remove = playersIndexedBySocketChannel.remove(socketAddress);
            if(remove == null){
                return false;
            }
            playersIndexedByUsername.remove(remove.getUsername());
            return true;
        }
    }

    public Player getPlayerBySocketAddress(SocketAddress address){
        return playersIndexedBySocketChannel.get(address);
    }

    public Player getPlayerByUsername(String username){
        return playersIndexedByUsername.get(username);
    }

    /**
     * concurrency safe
     * @param player player to add
     * @return if add success
     */
    public boolean addPlayer(Player player){
        synchronized (lock){
            if(playersIndexedByUsername.containsKey(player.getUsername())){
                return false;
            }else{
                playersIndexedByUsername.put(player.getUsername(),player);
                playersIndexedBySocketChannel.put(player.getSocketChannel().remoteAddress(),player);
                return true;
            }
        }
    }


    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            final TicTacServer s = this;
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ChannelInactiveHandler(s));
//                            pipeline.addLast(new IdleStateHandler(60,60, 0));
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            pipeline.addLast(new HttpRequestHandler("/ws", s));
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true));
                            pipeline.addLast(new TextWebSocketFrameInboundHandler(s));
                            pipeline.addLast(new LineBasedFrameDecoder(1024));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(8888).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

