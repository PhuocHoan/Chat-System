package com.haichutieu.chatsystem.server.util;

import java.nio.channels.AsynchronousSocketChannel;

public class ClientRequest {
    private AsynchronousSocketChannel clientChannel;
    private String request;

    public ClientRequest(AsynchronousSocketChannel clientChannel, String request) {
        this.clientChannel = clientChannel;
        this.request = request;
    }

    public AsynchronousSocketChannel getClientChannel() {
        return clientChannel;
    }

    public String getRequest() {
        return request;
    }
}
