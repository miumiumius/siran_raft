package yichen.yao.core.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import yichen.yao.core.rpc.client.handler.AppendEntriesResponseHandler;
import yichen.yao.core.rpc.client.handler.InstallSnapshotResponseHandler;
import yichen.yao.core.rpc.client.handler.VoteResponseHandler;
import yichen.yao.core.rpc.protocol.codec.RpcCodec;
import yichen.yao.core.rpc.protocol.codec.netty.NettyRequestCodec;
import yichen.yao.core.rpc.protocol.codec.netty.NettyRequestDecoder;
import yichen.yao.core.rpc.protocol.codec.netty.NettyRequestEncoder;
import yichen.yao.core.rpc.protocol.codec.netty.Spliter;
import yichen.yao.core.rpc.serialize.SerializerFactory;

import java.net.InetSocketAddress;

/**
 * @Author: siran.yao
 * @time: 2020/2/13:下午6:03
 */
public class NettyClient extends RpcClient {
    private String host;
    private int port;
    private RpcCodec rpcCodec;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        rpcCodec = new NettyRequestCodec(new SerializerFactory().getSerializer());
    }

    public void connection() {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            bootstrap
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new Spliter())
                                    .addLast(new NettyRequestDecoder(rpcCodec))
                                    .addLast(new VoteResponseHandler())
                                    .addLast(new AppendEntriesResponseHandler())
                                    .addLast(new InstallSnapshotResponseHandler())
                                    .addLast(new NettyRequestEncoder(rpcCodec))
                            ;
                        }
                    });
            ChannelFuture cf = bootstrap.connect(new InetSocketAddress(host,port)).sync();
            cf.channel().closeFuture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new NettyClient("localhost", 8080).connection();
    }
}
