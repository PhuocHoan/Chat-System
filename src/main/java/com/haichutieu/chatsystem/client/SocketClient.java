package com.haichutieu.chatsystem.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SocketClient {
    private AsynchronousSocketChannel client;
    private Future<Void> future;
    private static SocketClient instance;

    private SocketClient() {
        try {
            client = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 8080);
            future = client.connect(hostAddress);
            start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SocketClient getInstance() {
        if (instance == null)
            instance = new SocketClient();
        return instance;
    }

    private void start() {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String message) throws ExecutionException, InterruptedException {
        byte[] byteMsg = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        Future<Integer> writeResult = client.write(buffer);

        // do some computation

        writeResult.get();
        buffer.flip();
        Future<Integer> readResult = client.read(buffer);

        // do some computation

        readResult.get();
        String echo = new String(buffer.array()).trim();
        System.out.println(echo);
        buffer.clear();
        return echo;
    }

    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
