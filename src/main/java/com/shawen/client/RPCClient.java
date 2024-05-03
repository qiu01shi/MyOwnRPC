package com.shawen.client;


import com.shawen.common.RPCRequest;
import com.shawen.common.RPCResponse;

public interface RPCClient {
    RPCResponse sendRequest(RPCRequest request);
}
