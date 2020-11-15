package Chess.User;

import io.netty.channel.socket.SocketChannel;

import java.net.SocketAddress;

/**
 * @author jinaxCai
 */
public class Player {
    String username;
    SocketChannel socketChannel;

    public Player(String username, SocketChannel socketChannel) {
        this.username = username;
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
