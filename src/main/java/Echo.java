import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Echo {
    public static void main(String[] args) throws InterruptedException {
        start();
        startClient();
    }

    public static void start() throws InterruptedException {
        EchoHandler handler = new EchoHandler();
        EventLoopGroup group = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(9999))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handler);
                    }
                });

        ChannelFuture future = serverBootstrap.bind().sync();
        //  future.channel().closeFuture().sync();
    }

    public static void startClient() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress("127.0.0.1", 9999)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new clientHandler());
                    }
                });
        ChannelFuture future = b.connect().sync();
        ChannelFuture channelFuture = future.channel().writeAndFlush("aaaaa".getBytes());
        channelFuture.addListener(f->{
            if (f.isSuccess()){
                System.out.println("成功");
            }else {
                System.out.println("失败");
            }
        });

        future.channel().closeFuture().sync();

    }


    public static class EchoHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            if (in.hasArray()) {//使用堆内存
                System.out.println("收到消息：" + new String(in.array(), CharsetUtil.UTF_8));
            } else {//使用直接内存或者复合缓冲区
                int readableBytes = in.readableBytes();
                byte[] b = new byte[readableBytes];
                in.getBytes(in.readerIndex(), b);
                System.out.println("收到消息：" + new String(b, CharsetUtil.UTF_8));
            }
            ctx.write(in);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            System.out.println("建立链接");
        }
    }


    public static class clientHandler extends SimpleChannelInboundHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf in) throws Exception {
            if (in.hasArray()) {//使用堆内存
                System.out.println("收到消息："+new String(in.array(), CharsetUtil.UTF_8) + "-redisindex:" + in.readerIndex());
            } else {//使用直接内存或者复合缓冲区
                int readableBytes = in.readableBytes();
                byte[] b = new byte[readableBytes];
                in.readBytes(b);
                System.out.println("收到消息："+new String(b, CharsetUtil.UTF_8) + "-redisindex:" + in.readerIndex());
            }
        }
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            //ctx.writeAndFlush(Unpooled.copiedBuffer("hello", CharsetUtil.UTF_8));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println(cause);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("链接关闭");
        }
    }


}
