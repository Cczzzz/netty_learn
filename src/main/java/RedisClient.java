import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.redis.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.List;

public class RedisClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b =new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress("127.0.0.1",6379)
                .handler(new RedisClientInitializer());
        ChannelFuture future = b.connect().sync();

        ChannelFuture writeAndFlush = future.channel().writeAndFlush("get name ");
        writeAndFlush.addListener(f->{
           if (f.isSuccess()){
               System.out.println("成功");
           }
        });
        //System.out.println("zzzz");
    }



    public static class redisHandler extends ChannelDuplexHandler{
        // 发送 redis 命令
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            String[] commands = ((String) msg).split("\\s+");
            List<RedisMessage> children = new ArrayList<>(commands.length);
            for (String cmdString : commands) {
                children.add(new FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ctx.alloc(), cmdString)));
            }
            RedisMessage request = new ArrayRedisMessage(children);
            ctx.write(request, promise);

        }

        // 接收 redis 响应数据
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            RedisMessage redisMessage = (RedisMessage) msg;
            // 打印响应消息
            printAggregatedRedisResponse(redisMessage);
            // 是否资源
            ReferenceCountUtil.release(redisMessage);
        }

        private static void printAggregatedRedisResponse(RedisMessage msg) {
            if (msg instanceof SimpleStringRedisMessage) {
                System.out.println(((SimpleStringRedisMessage) msg).content());
            } else if (msg instanceof ErrorRedisMessage) {
                System.out.println(((ErrorRedisMessage) msg).content());
            } else if (msg instanceof IntegerRedisMessage) {
                System.out.println(((IntegerRedisMessage) msg).value());
            } else if (msg instanceof FullBulkStringRedisMessage) {
                System.out.println(getString((FullBulkStringRedisMessage) msg));
            } else if (msg instanceof ArrayRedisMessage) {
                for (RedisMessage child : ((ArrayRedisMessage) msg).children()) {
                    printAggregatedRedisResponse(child);
                }
            } else {
                throw new CodecException("unknown message type: " + msg);
            }
        }

        private static String getString(FullBulkStringRedisMessage msg) {
            if (msg.isNull()) {
                return "(null)";
            }
            return msg.content().toString(CharsetUtil.UTF_8);
        }

    }


    public static class RedisClientInitializer extends ChannelInitializer<Channel>{

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new RedisDecoder());
            pipeline.addLast(new RedisBulkStringAggregator());
            pipeline.addLast(new RedisArrayAggregator());
            pipeline.addLast(new RedisEncoder());
            pipeline.addLast(new redisHandler());
        }
    }



}


