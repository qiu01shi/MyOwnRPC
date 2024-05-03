package com.shawen.server;

import com.shawen.common.RPCRequest;
import com.shawen.common.RPCResponse;
import com.shawen.common.RpcMessage;
import com.shawen.common.enums.CompressTypeEnum;
import com.shawen.common.enums.SerializationTypeEnum;
import com.shawen.common.RpcConstants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 因为是服务器端，我们知道接受到请求格式是RPCRequest
 * Object类型也行，强制转型就行
 */
@Slf4j
@AllArgsConstructor
public class NettyRPCServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private ServiceProvider serviceProvider;

    // 是 SimpleChannelInboundHandler 的核心方法，用于处理接收到的每个 RPCRequest 消息。
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
            log.info("server receive msg: [{}] ", msg);
            byte messageType = msg.getMessageType();
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                rpcMessage.setData(RpcConstants.PONG);
            } else {
                RPCRequest rpcRequest = (RPCRequest) msg.getData();
                RPCResponse response = getResponse(rpcRequest);
                log.info(String.format("server get result: %s", response.toString()));
                rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                rpcMessage.setData(response);
            }
            ctx.writeAndFlush(rpcMessage);
            ctx.close();
    }

    // 这个方法在处理过程中发生异常时被调用。
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    RPCResponse getResponse(RPCRequest request) {
        // 得到服务名
        String interfaceName = request.getInterfaceName();
        // 得到服务端相应服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用方法
        Method method = null;
        try {
            // 根据请求中的方法名和参数类型获取service对象的相应方法。
            method = service.getClass().getMethod(request.getMethodName(), request.getParamsTypes());
            // 调用方法，并传入参数。
            Object invoke = method.invoke(service, request.getParams());
            return RPCResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RPCResponse.fail();
        }
    }
}
