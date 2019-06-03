import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBuf2 {
    public static void main(String[] args) {
        io.netty.buffer.ByteBuf bufs = Unpooled.buffer(10,1000);
        System.out.println(bufs.maxCapacity());
        System.out.println(bufs.maxWritableBytes());
        System.out.println(bufs.capacity());

        System.out.println("索引："+bufs.readerIndex()+":"+bufs.writerIndex());
        System.out.println("可写："+bufs.writableBytes()+":"+bufs.readableBytes());
        bufs.writeBytes("hello".getBytes());
        System.out.println("索引："+bufs.readerIndex()+":"+bufs.writerIndex());
        System.out.println("可写："+bufs.writableBytes()+":"+bufs.readableBytes());
        bufs.writeBytes("hello1111111".getBytes());
        System.out.println("索引："+bufs.readerIndex()+":"+bufs.writerIndex());
        System.out.println("可写："+bufs.writableBytes()+":"+bufs.readableBytes());
    }
}
