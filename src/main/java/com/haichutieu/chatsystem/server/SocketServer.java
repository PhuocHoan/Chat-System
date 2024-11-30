package com.haichutieu.chatsystem.server;

import com.haichutieu.chatsystem.server.util.ClientRequest;
import com.haichutieu.chatsystem.server.util.RequestController;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;

public class SocketServer {
    private AsynchronousServerSocketChannel serverChannel;
    private BlockingQueue<ClientRequest> requestQueue = new LinkedBlockingQueue<>();
    final String HOST = "localhost";
    final int PORT = 8080;

    // Used for handling multiple clients
//    private Set<AsynchronousSocketChannel> clients = Collections.synchronizedSet(new HashSet<>());

    public SocketServer() {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress(HOST, PORT);
            serverChannel.bind(hostAddress);
            ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            System.out.println("Server started at " + HOST + ":" + PORT);

            // Start a thread to process requests from the queue
            new Thread(() -> {
                while (true) {
                    try {
                        ClientRequest request = requestQueue.take();
                        RequestController.handleRequest(request);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runServer() {
        try {
            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                    System.out.println("New client connected: " + clientChannel);

                    // Start reading from this client
                    readFromClient(clientChannel);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.err.println("Failed to accept connection: " + exc.getMessage());
                }
            });

            // Keep the server running
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void readFromClient(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (result == -1) {
                    disconnectClient(clientChannel);
                    return;
                }

                buffer.flip();
                String request = new String(buffer.array(), 0, buffer.limit());
                System.out.println("Received: " + request);

                // Push the request to the queue
                requestQueue.add(new ClientRequest(clientChannel, request));

                // Clear buffer and continue reading
                buffer.clear();
                clientChannel.read(buffer, buffer, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.err.println("Failed to read from client: " + exc.getMessage());
                disconnectClient(clientChannel);
            }
        });
    }

    public void sendMessageToClient(String message) {

    }

//    public void broadcastMessage(String message) {
//        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
//        synchronized (clients) {
//            for (AsynchronousSocketChannel client : clients) {
//                client.write(buffer.duplicate(), buffer.duplicate(), new CompletionHandler<Integer, ByteBuffer>() {
//                    @Override
//                    public void completed(Integer result, ByteBuffer buffer) {
//                        // Message successfully sent
//                    }
//
//                    @Override
//                    public void failed(Throwable exc, ByteBuffer buffer) {
//                        System.err.println("Failed to send message: " + exc.getMessage());
//                        disconnectClient(client);
//                    }
//                });
//            }
//        }
//    }

    private void disconnectClient(AsynchronousSocketChannel clientChannel) {
        try {
            System.out.println("Client disconnected: " + clientChannel);
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

