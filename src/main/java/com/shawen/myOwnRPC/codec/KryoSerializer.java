package com.shawen.myOwnRPC.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.shawen.myOwnRPC.common.RPCRequest;
import com.shawen.myOwnRPC.common.RPCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerializer implements Serializer{

    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    /**
     * 由于 Kryo 不是线程安全的。每个线程都应该有自己的 Kryo，Input 和 Output 实例。
     * 所以，使用 ThreadLocal 存放 Kryo 对象
     */

    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RPCResponse.class);
        kryo.register(RPCRequest.class);
        kryo.setReferences(true);//默认值为true,是否关闭注册行为,关闭之后可能存在序列化问题，一般推荐设置为 true
        kryo.setRegistrationRequired(false);//默认值为false,是否关闭循环引用，可以提高性能，但是一般不推荐设置为 true
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // Object->byte:将对象序列化为byte数组
            kryo.writeObject(output, obj);
            bytes = output.toBytes();
            kryoThreadLocal.remove();
        } catch (IOException e) {
            logger.error("occur exception when serialize:", e);
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            switch (messageType){
                case 0:
                    RPCRequest rpcRequest = kryo.readObject(input, RPCRequest.class);
                    kryoThreadLocal.remove();
                    obj = rpcRequest;
                    break;
                case 1:
                    RPCResponse rpcResponse = kryo.readObject(input, RPCResponse.class);
                    kryoThreadLocal.remove();
                    obj = rpcResponse;
                    break;
            }

        }catch (Exception e) {
            logger.error("occur exception when deserialize:", e);
            e.printStackTrace();
        }
        return obj;
    }

    // 2 代表 KryoSerializer 序列化方式
    @Override
    public int getType() {
        return 2;
    }
}
