package com.shawen.myOwnRPC.codec;

import com.shawen.myOwnRPC.codec.serializer.Serializer;
import com.shawen.myOwnRPC.common.enums.CompressTypeEnum;
import com.shawen.myOwnRPC.common.RpcConstants;
import com.shawen.myOwnRPC.common.RpcMessage;
import com.shawen.myOwnRPC.common.enums.SerializationTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * todo 没有 压缩类型
 *
 * </pre>
 */

// todo 暂时不用 Compress 及 ExtensionLoader

@AllArgsConstructor
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private Serializer serializer;

    //    这是一个静态原子整数，用于线程安全地生成序列号，每次调用getAndIncrement()时都会增加。
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    //    ctx: ChannelHandlerContext，Netty中处理I/O操作和拦截I/O事件的上下文。
    //    rpcMessage: 待编码的RPC消息对象。
    //    out: ByteBuf，Netty中的数据容器，用于存储编码后的字节。
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        try {
            // 1. 写入魔数和版本号:
            out.writeBytes(RpcConstants.MAGIC_NUMBER);  // 写入用于识别数据包的魔数。
            out.writeByte(RpcConstants.VERSION);    // 写入协议版本号。

            // 2. 预留空间写入完整长度:
            // 在当前写入索引处预留4个字节的空间来后续写入消息的完整长度。
            out.writerIndex(out.writerIndex() + 4);

            // 3. 写入消息类型、编解码器;
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            out.writeByte(rpcMessage.getCodec());   // Serialize 类型
            out.writeByte(CompressTypeEnum.GZIP.getCode()); // compress 类型 待用
            out.writeInt(ATOMIC_INTEGER.getAndIncrement()); // 写入序列号

            // build full length
            // 4. 序列化和压缩消息体:
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;

            // 如果消息类型不是心跳消息，序列化消息体; fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {

                // 获取序列化方式
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("codec name: [{}] ", codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData()); // 序列化数据
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            int writeIndex = out.writerIndex();
            // 调整写入索引到头部后的位置，写入完整长度的值。
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}