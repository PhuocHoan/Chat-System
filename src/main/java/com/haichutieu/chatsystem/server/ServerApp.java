package com.haichutieu.chatsystem.server;

public class ServerApp {
    public static void main(String[] args) throws InterruptedException {
        SocketServer server = new SocketServer();
        server.runServer();
    }
}