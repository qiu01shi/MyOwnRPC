package com.shawen.myOwnRPC.client;


import com.shawen.myOwnRPC.common.RPCRequest;
import com.shawen.myOwnRPC.common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request);
}
