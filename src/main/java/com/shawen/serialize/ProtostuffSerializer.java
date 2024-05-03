package com.shawen.serialize;

import com.shawen.common.RPCRequest;
import com.shawen.common.RPCResponse;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializer implements Serializer{

    /**
     * Avoid reapplying buffer space every time serialization
     */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    public byte[] serialize(Object obj) {
        Class<?> clazz = obj.getClass();
        Schema schema = RuntimeSchema.getSchema(clazz);
        byte[] bytes = null;
        try {
            bytes = ProtostuffIOUtil.toByteArray(obj, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        switch (messageType){
            case 0:
                Schema<RPCRequest> rpcRequestSchema = RuntimeSchema.getSchema(RPCRequest.class);
                RPCRequest rpcRequest = rpcRequestSchema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes, rpcRequest, rpcRequestSchema);
                obj = rpcRequest;
                break;
            case 1:
                Schema<RPCResponse> rpcResponseSchema = RuntimeSchema.getSchema(RPCResponse.class);
                RPCResponse rpcResponse = rpcResponseSchema.newMessage();
                ProtostuffIOUtil.mergeFrom(bytes, rpcResponse, rpcResponseSchema);
                obj = rpcResponse;
                break;
            default:
                System.out.println("暂时不支持此种消息");
                throw new RuntimeException();
        }
        return obj;
    }
}
