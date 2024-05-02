package com.shawen.myOwnRPC.client;

import com.shawen.myOwnRPC.common.RPCResponse;
import com.shawen.myOwnRPC.common.RpcConstants;
import com.shawen.myOwnRPC.common.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        // 接收到response, 给channel设计别名，让 sendRequest 中可以读取 response
        // 创建一个 AttributeKey 对象，这是 Netty 用于在通道中存储特定信息的机制。
        // 这里的键名是"RPCResponse"。
        byte messageType = msg.getMessageType();
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            log.info("heart [{}]", msg.getData());
        } else if (messageType == RpcConstants.RESPONSE_TYPE) {
            RPCResponse rpcResponse = (RPCResponse) msg.getData();
            AttributeKey<RPCResponse> key = AttributeKey.valueOf("RPCResponse");
            // 将接收到的 RPC 响应 msg 设置到通道的属性中。
            // 这样，发送 RPC 请求的客户端可以从同一个通道的属性中检索到 RPC 响应。
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
